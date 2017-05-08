using System.Collections.Generic;
using Assets.Scripts.Extensions;
using Assets.Scripts.MeshGeneration;
using Assets.Scripts.Util;
using Improbable.Unity.Visualizer;
using Improbable.ExtrudedPolygon;
using UnityEngine;

namespace Assets.Scripts.Buildings
{
    public class BuildingMeshVisualizer : PolygonMeshVisualizer
    {
        [Require] public ExtrudedPolygonData.Reader ExtrudedPolygonReader;

        protected override List<Vector3> GetVertices()
        {
            var baseVertices = new List<Vector3>();
            for (var i = 0; i < ExtrudedPolygonReader.Data.polygonBaseVertices.Count; i++) {
                var vertex = ExtrudedPolygonReader.Data.polygonBaseVertices[i];
                baseVertices.Add(vertex.ScaleWorldToUnityVector(StudyAreaMapping.WorldScaleFactor));
            }
            return baseVertices;
        }

        protected override float GetThickness()
        {
            return ExtrudedPolygonReader.Data.height;
        }
    }
}