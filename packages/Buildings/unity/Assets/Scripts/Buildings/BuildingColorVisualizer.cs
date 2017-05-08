using System;
using UnityEngine;
using Improbable;
using SpatialOS = Improbable.Unity.Core.SpatialOS;
using Improbable.Unity.Visualizer;
using Improbable.Unity.Core.EntityQueries;
using Improbable.Worker;
using Improbable.Collections;
using Improbable.Position;
using System.Collections.Generic;
using Improbable.Math;
using Assets.Scripts.Util;
using Assets.Scripts.ColorSelection;
using Assets.Scripts.Navigation;
using Improbable.GlobalConfig;
using Improbable.RoadNetworkRouting;

namespace Assets.Scripts.Buildings
{
    class BuildingColorVisualizer : IColorVisualizer
    {
        [Require] 
        private CanonicalPosition.Reader BuildingPosition;

        public Renderer RendererToUpdate = null;

        public Material DefaultMaterial = null;
        public Material SelectedMaterial = null;
        public Material InactiveMaterial = null;

        private ColorPulser _callingAllPeoplePulser;
        
        private bool _selected = false;
        private bool _currentDestinationOverrideSet = false;
        
        [HideInInspector]
        public bool Disabled = false;

        public void Awake()
        {
            var startColor = SelectedMaterial.color;
            var endColor = Color.Lerp(startColor, Color.white, 0.5f);
            _callingAllPeoplePulser = new ColorPulser(0.5f, startColor, endColor, SelectedMaterial);
        }

        public void Update()
        {
            SelectColor();

            if (_selected && Input.GetKeyDown(KeyCode.K))
            {
                findNearestJunctionToBuildingAndSetAsCurrentDestinationOverride(200.0);
            }
            
            if (_currentDestinationOverrideSet)
            {
                _callingAllPeoplePulser.Update();
            }
        }

        public override void Highlight()
        {
            _selected = true;
        }

        public override void RemoveHighlight()
        {
            _selected = false;
            _currentDestinationOverrideSet = false;
        }

        private void SelectColor()
        {
            if (_selected)
            {
                RendererToUpdate.sharedMaterial = SelectedMaterial;
            }
            else if (Disabled)
            {
                RendererToUpdate.sharedMaterial = InactiveMaterial;
            }
            else
            {
                RendererToUpdate.sharedMaterial = DefaultMaterial;
            }
        }

        private void findNearestJunctionToBuildingAndSetAsCurrentDestinationOverride(double range)
        {
            
            var query = Query.And(
                            Query.HasComponent<NavPointRoutingInfo>(),
                            Query.InSphere(transform.position.x, transform.position.y, transform.position.z, range)
            ).ReturnComponents(CanonicalPosition.ComponentId, NavPointRoutingInfo.ComponentId);

            Debug.Log("Querying for nearest junction to building: " + gameObject.EntityId());

            SpatialOS.WorkerCommands.SendQuery(query, result =>
                {
                    if (result.StatusCode != StatusCode.Success)
                    {
                        Debug.Log("Query failed with error: " + result.ErrorMessage);
                        return;
                    }

                    if (result.Response.HasValue)
                    {
                        
                        if (result.Response.Value.EntityCount < 1)
                        {
                            Debug.Log("Found " + result.Response.Value.EntityCount + " nearby entities with a IncomingTrafficRules component");
                            return;
                        }
                            
                        setNearestJunctionAsCurrentDestinationOverride(result.Response.Value.Entities);
                    }
                });

        }

        private void setNearestJunctionAsCurrentDestinationOverride(Map<EntityId, Entity> junctionMap)
        {
            
            Coordinates? nearestJunctionPosition = null;
            NavPointRoutingInfoData? nearestJunctionRoutingInfo = null;
            long nearestEntityId = -1;
            double nearestDistance = Double.MaxValue;

            foreach (KeyValuePair<EntityId, Entity> kvp in junctionMap)
            {
                var entityPositionMaybe = kvp.Value.Get<CanonicalPosition>();
                var entitySectorInfoMaybe = kvp.Value.Get<NavPointRoutingInfo>();

                if (entityPositionMaybe.HasValue && entitySectorInfoMaybe.HasValue)
                {
                    var entityPosition = entityPositionMaybe.Value.Get().Value.position;
                    var entitySectorInfo = entitySectorInfoMaybe.Value.Get().Value;
                    var distance = CoordinateUtil.Distance(entityPosition, BuildingPosition.Data.position);

                    if (distance < nearestDistance)
                    {
                        nearestDistance = distance;
                        nearestJunctionPosition = entityPosition;
                        nearestJunctionRoutingInfo = entitySectorInfo;
                        nearestEntityId = kvp.Key.Id;
                    }
                }
            }

            if (nearestEntityId != -1 && nearestJunctionPosition.HasValue && nearestJunctionRoutingInfo.HasValue)
            {
                Debug.Log("Query found nearest junction id: " + nearestEntityId);
                SetAsCurrentDestinationOverride(nearestEntityId, nearestJunctionPosition.Value, nearestJunctionRoutingInfo.Value);
            }
        }

        private void SetAsCurrentDestinationOverride(long entityId, Coordinates navPointPosition, NavPointRoutingInfoData navPointRoutingInfo)
        {            
            if (CurrentDestinationOverrideGlobalWriter.IsInitialized)
            {
                var update = new CurrentDestinationOverrideDetails.Update()
                    .SetNavPointEntityId(new EntityId(entityId))
                    .SetPosition(navPointPosition);
                
                CurrentDestinationOverrideGlobalWriter.CurrentDestinationOverrideDetailsWriterInstance.Send(update);

                _currentDestinationOverrideSet = true;
            }
        }
    }
}

