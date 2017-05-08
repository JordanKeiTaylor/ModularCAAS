package preprocessors.osm

import core._
import core.config.GeneratorConfig
import core.coordinates.SimulationCoordinates
import core.file.ResourcesUtil
import core.settings.{Configuration, PackageNames, PrefabNames}
import improbable.math.Vector3f
import preprocessors.osm._
import core.coordinates.CoordinateUtil
import preprocessors.buildings.BuildingData
import preprocessors.osm.{OsmPolygonData, ProcessorUtil}


import scala.util.Random

class OsmBuildingConverter(config: GeneratorConfig,
                           osmData: OsmData) extends Preprocessor {

  var packageDependencies: List[String] = List[String]("OSMData")
  var componentDependencies: List[Integer] = List[Integer]()
  var prefabDependencies: List[PrefabNames.Value] = List[PrefabNames.Value]()

  var packageName: PackageNames.Value = PackageNames.Buildings
  var prefabsInstantiated: List[PrefabNames.Value] = List(PrefabNames.Building)
  var componentsInstantiated: List[Int] = List()

  private val BuildingStoryHeight = 2.5f
  private val RoofHeight = 0.7f
  private val BuidingAreaStoryHeight = 350
  private val MinBuildingHeightStoreys = 1
  private val StoryStandardDeviation = 1.5

  private val OptimizeBuildingsMinVertexDelta = 1.2

  private var _buildingsByOsmWayId = Map.empty[Long, BuildingData]

  private val rand = Random

  lazy val supportedOSMTags: Map[String, Set[String]] = {
    ResourcesUtil.readAsJson[Map[String, Set[String]]](Configuration.osmBuildingsTagsJsonFilePath)
  }

  def buildingsByOsmWayId: Map[Long, BuildingData] = _buildingsByOsmWayId

  var buildings :Seq[BuildingData] = Seq.empty

  override def start(progress: ProgressLogger): Unit = {
    buildings = convert(osmData)
  }

  override def snapshot(progressLogger: ProgressLogger, snapshot: SnapshotBuilder): Unit = {
    progressLogger.info(s"OsmBuildingConverter: Serializing Data for ${buildings.size} buildings")
    buildings.foreach(_.export(snapshot, PrefabNames.Building))
  }

  private def convert(osmData: OsmData) = {
    val buildingsWaysWithValidTags = getMultiBuildingsWays(osmData) ++ getRegularBuildingsWays(osmData);

    val buildings = getBuildingsFromWays(buildingsWaysWithValidTags)

    buildings
  }

  private def getRegularBuildingsWays(osmData: OsmData): Iterable[OsmWay] = {
    osmData.osmWays.ways.values
      .filter(ProcessorUtil.osmWayContainsSupportedTag(_, supportedOSMTags))
  }

  private def getMultiBuildingsWays(osmData: OsmData): List[OsmWay] = {
    val multiBuildingRelations = osmData.osmRelations.relations.filter(ProcessorUtil.osmRelationContainsTag(_, RawTag("building", "yes")))

    multiBuildingRelations.flatMap(multibuilding => {
      multibuilding.members
        .filter(member => member.memberType == "way" && member.role == "outer")
        .flatMap(member => osmData.osmWays.ways.values.find(_.id == member.id))
    })
  }

  private def getBuildingsFromWays(buildingWays : Iterable[OsmWay]): Seq[BuildingData] =  {
    buildingWays.flatMap {
      osmWay =>
        val maybeBuilding = createBuilding(osmWay, osmData.osmNodes, config)
        maybeBuilding.foreach(building => _buildingsByOsmWayId += (osmWay.id -> building))
        maybeBuilding
    }.toSeq
  }

  private def generateCoordinateTriples(vertices: List[SimulationCoordinates]): Seq[(SimulationCoordinates, SimulationCoordinates, SimulationCoordinates)] = {
    val count = vertices.length
    for (index <- 0 until count)
      yield (vertices(index), vertices((index + 1) % count), vertices((index + 2) % count))
  }

  def scoreTriple(triple: (SimulationCoordinates, SimulationCoordinates, SimulationCoordinates)): Double = {
    val z: SimulationCoordinates = triple._1 - triple._2
    val n: SimulationCoordinates = triple._3 - triple._1
    val closest: SimulationCoordinates = z - (n * z.dot(n))
    closest.magnitude
  }

  private def optimiseWayNodes(vertices: List[SimulationCoordinates], threshold: Double): List[SimulationCoordinates] = {
    if (vertices.length <= 3) {
      vertices
    } else {
      val triples = generateCoordinateTriples(vertices)
      val minChange = triples.minBy(scoreTriple)
      val score = scoreTriple(minChange)
      if (score <= threshold) {
        optimiseWayNodes(vertices.diff(List(minChange._2)), threshold)
      } else {
        vertices
      }
    }
  }

  private def createBuilding(osmWay: OsmWay, osmNodes: OsmNodes, config: GeneratorConfig): Option[BuildingData] = {
    val nodes = createCoordinatesOfWayNodes(osmWay, osmNodes)
    val optimisedNodes = optimiseWayNodes(nodes, OptimizeBuildingsMinVertexDelta)
    val dynamicShapeData = OsmPolygonData(osmWay, optimisedNodes)
    val position = CoordinateUtil.mean(dynamicShapeData.coordinates).get
    val verticesEntityRelativeCoordinates = verticesToEntityRelativeCoordinates(position, dynamicShapeData.coordinates)
    val height = parseOrGenerateHeight(osmWay, osmNodes)

    Some(BuildingData(
      position,
      verticesEntityRelativeCoordinates,
      height
    ))
  }

  private def verticesToEntityRelativeCoordinates(position: SimulationCoordinates, vertices: List[SimulationCoordinates]): List[Vector3f] = {
    vertices.map(v => (v - position).toVector3f)
  }

  private def parseOrGenerateHeight(osmWay: OsmWay, osmNodes: OsmNodes): Float = {
    getParsedHeight(osmWay).getOrElse(generateProceduralHeight(osmWay, osmNodes))
  }

  private def generateProceduralHeight(osmWay: OsmWay, osmNodes: OsmNodes): Float = {
    val area = getBoundingBoxAreaOfWay(osmWay, osmNodes).toFloat
    generateHeightFromArea(area)
  }

  private def generateHeightFromArea(buildingArea: Float): Float = {
    var numStoreys = buildingArea match {
      case area if area < BuidingAreaStoryHeight => 1
      case area if area >= BuidingAreaStoryHeight && area < 2 * BuidingAreaStoryHeight => 2
      case area if area >= 2 * BuidingAreaStoryHeight && area < 3 * BuidingAreaStoryHeight => 3
      case area if area >= 3 * BuidingAreaStoryHeight => 4
      case _ => 2
    }

    val storeyOffset = randomStoreyOffset()
    numStoreys += storeyOffset
    numStoreys = if (numStoreys > MinBuildingHeightStoreys) numStoreys else MinBuildingHeightStoreys

    numStoreys * BuildingStoryHeight + RoofHeight
  }

  private def randomStoreyOffset(): Int = {
    (rand.nextGaussian().asInstanceOf[Float] * StoryStandardDeviation).toInt
  }

  private def createCoordinatesOfWayNodes(osmWay: OsmWay, osmNodes: OsmNodes): List[SimulationCoordinates] = {
    osmWay.nodeIds.map {
      nodeId =>
        config.completeTransform(osmNodes.nodes(nodeId).toGeo)
    }
  }

  private def getParsedHeight(osmWay: OsmWay): Option[Float] = {
    osmWay.tags.get("height").map(_.head.replaceAll("[^0-9\\.\\-]", "").toFloat)
  }

  private def getBoundingBoxAreaOfWay(osmWay: OsmWay, osmNodes: OsmNodes): Double = {
    val nodes = osmWay.nodeIds.map {
      osmNodes.nodes(_)
    }
    val latCoordinates = nodes.map {
      _.lat
    }
    val lonCoordinates = nodes.map {
      _.lon
    }

    val centreLon = (lonCoordinates.min + lonCoordinates.max) / 2
    val centreLat = (latCoordinates.min + latCoordinates.max) / 2

    val latDistance = CoordinateUtil.calculateDistanceBetweenGeoCoordinates(latCoordinates.min, centreLon, latCoordinates.max, centreLon)
    val lonDistance = CoordinateUtil.calculateDistanceBetweenGeoCoordinates(centreLat, lonCoordinates.min, centreLat, lonCoordinates.max)

    latDistance * lonDistance
  }

}
