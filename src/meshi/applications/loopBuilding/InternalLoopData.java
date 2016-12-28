package meshi.applications.loopBuilding;

class InternalLoopData {
		double evEnergy;
		double propEnergy;
		double bbHBenergy;
		double RMS;

		public InternalLoopData() {}

	public InternalLoopData(double evEnergy,
		 double propEnergy,
		 double bbHBenergy,
		 double RMS) {
			this.evEnergy = evEnergy;
			this.propEnergy = propEnergy;
			this.bbHBenergy = bbHBenergy;
			this.RMS = RMS;
		}
		
}
