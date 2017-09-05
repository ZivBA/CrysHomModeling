package meshi.energy.hydrogenBondsPlane;

import meshi.util.MeshiList;

/**
 **/
class CNtwoDistancesList extends MeshiList  {
    protected CNtwoDistancesList(int capacity) {
           super(new CNtwoDistances.IsCNtwoDistances(),capacity);
       }

    CNtwoDistancesList() {
           super(new CNtwoDistances.IsCNtwoDistances());
       }
}

