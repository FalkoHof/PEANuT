package age.mpg.de.peanut.statistics;

import org.apache.commons.math3.distribution.HypergeometricDistribution;


//class for calculating a one sided Fisher's exact test based on the hypergeometric distribution
public class HypergeometricDist {
		

		private int numberOfSuccesses;
		private int populationSize;
		private int numberOfSuccessesInPopulation;
		private int sampleSize;
		
		
		public HypergeometricDist(int pathwaySize, int parentNetworkSize, int foundPathwayMembers, int childNetworkSize){
			
			this.populationSize = parentNetworkSize;
			this.numberOfSuccessesInPopulation = pathwaySize;
			this.numberOfSuccesses = pathwaySize - foundPathwayMembers;
			this.sampleSize = parentNetworkSize - childNetworkSize;
		}
		
		
		
		public double computeOneTailedFisher(){	
			HypergeometricDistribution hd = new HypergeometricDistribution(populationSize, numberOfSuccessesInPopulation, sampleSize);		
			return hd.cumulativeProbability(numberOfSuccesses);
		}
}
