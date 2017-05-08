package preprocessors.buildings

import core.coordinates.SimulationCoordinates
import core.settings.Prefabs
import improbable.{EntityAcl, EntityAclData}
import improbable.extruded_polygon.{ExtrudedPolygonDataData, ExtrudedPolygonData}
import improbable.position.{CanonicalPosition, CanonicalPositionData}
import improbable.math.Vector3f
import improbable.worker.SnapshotEntity
import workers.Workers
import core.{AclBuilder, Snapshotable}

import scala.collection.JavaConverters._
import scala.util.Random


case class BuildingData(localCoords: SimulationCoordinates,
                        vertices: List[Vector3f], height: Float) extends Snapshotable {

  override def buildForSnapshot(): SnapshotEntity = {
    val entity = new SnapshotEntity(Prefabs.Building)
    entity.add[ExtrudedPolygonDataData,ExtrudedPolygonData](classOf[ExtrudedPolygonData],
      new ExtrudedPolygonDataData(vertices.asJava, height))
    entity.add[CanonicalPositionData,CanonicalPosition](classOf[CanonicalPosition],
      new CanonicalPositionData(localCoords.toPositionCoordinates))

    var entityAcl = buildAcl(Set(Workers.client))

    entity.add[EntityAclData,EntityAcl](classOf[EntityAcl], entityAcl.build())

    entity
  }

  def chooseOne[T](list: List[T]): T = {
    val index = Math.floor(list.size * Random.nextDouble()).toInt
    list(index)
  }

  def scaleBy(input: Vector3f, scale: Float) = new Vector3f(input.getX * scale, input.getY * scale, input.getZ * scale)

}
