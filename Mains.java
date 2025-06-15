package application;


public class Mains {

//Problem specification:
public static final int INPUT_DIMENSIONALITY = 15;
public static final int LOWEST_INPUT = -2;
public static final int HIGHEST_INPUT = 2;
/****************************************************/

//Simulated Annealing parameters:
public static final double INITIAL_TEMPERATURE = 1000;//it can be any different positive value since we are adjusting the cooling rate to suite it anyway
public static final int ITERATIONS_NUMBER = 1000000; //increase for better results but longer processing time ;)
public static final double EXPLORATION_PERCENTAGE = 0.87;//the need of this variable is demonstrated below
public static final double COOLING_RATE = calculateCoolingRateThatProvideACertainDistribution();

//To obtain good results out of Simulated Annealing we should understand that it goes through two phases
//1- Exploration Phase: which is the phase that the algorithm explore the solution space and get out of local minima
//2- Exploitation phase: which is the phase that the algorithm start exploiting towards the end, the algorithm start being greedy at this phase, and it works just like hill climbing
//For best distribution between Exploration Phase and Exploitation phase
//Empirical and Theoretical Guidelines suggest:
//(80%-90%) of the iterations be spent in the exploration phase
//(10%-20%) of the iterations be spent in the exploitation phase
//So just increasing the number of iterations doesn't really get the best of simulated annealing
//You need to Keep a good distribution of iterations between Exploration And Exploitation
//Otherwise increasing the iteration number is just increasing the Exploitation part and your algorithm is going greedy
//To increase the Exploration you can slow the cooling down by Increasing the cooling rate(but increasing it too much will cause that the temperature never get to zero so it will kill the Exploitation phase)
//And To increase the Exploitation you can increase the number of iterations or just decrease the cooling rate to fasten the cooling up
//In light of this we created a method to calculate the best cooling rate that provides the wanted ratio between the two phases to get the best results always easily
/*****************************************************************************************************************/

//Neighbor-choosing related variables:
public static final int NUMBER_OF_INPUTS_TO_MUTATE_FOR_NEIGHBOR = 5;
public static final double MUTATION_FACTOR = 0.9;
/*****************************************************************************************************************/
public static void main(String[] args) {
    SimulatedAnnealingResult result = simulatedAnnealing();
    System.out.println(result.getFinalResult());
}

private static SimulatedAnnealingResult simulatedAnnealing(){

    double temperature = INITIAL_TEMPERATURE;

    //At first, we create a random set of inputs and choose it to be the solution.
    double [] solution = getRandomInput();
    //we calculate it's cost
    double solutionCost = calculateCost(solution);

    double [] iterationsResults = new double[ITERATIONS_NUMBER];//here we will store the results of every iteration to diagram the progress

    for(int i = 0 ; i < ITERATIONS_NUMBER ; i++){
        //we choose a neighbor of our solution
        double [] neighborSolution = getNeighbor(solution, temperature);
        //we calculate the neighbor's cost
        double neighborCost = calculateCost(neighborSolution);

        if(neighborCost <= solutionCost){//if the neighbor has less cost we accept it as our new solution
            solution = neighborSolution;
            solutionCost = neighborCost;
        }
        else{//if the neighbor have Greater cost we accept it by "boltzmann probability"
            double boltzmannProbability = Math.exp( ( -(Math.abs(solutionCost - neighborCost) ) / temperature) );

            //The following code is performing an action based on probability (boltzmann probability)
            double randomNumber = Math.random();
            if(randomNumber <= boltzmannProbability){
                solution = neighborSolution;
                solutionCost = neighborCost;
            }
        }
        iterationsResults[i] = solutionCost;
        temperature = temperature * COOLING_RATE;//We cool the system at the end of each iteration
    }

    //we return the simulated annealing results
    SimulatedAnnealingResult simulatedAnnealingResult = new SimulatedAnnealingResult(iterationsResults, solutionCost);
    return simulatedAnnealingResult;
}


private static double calculateCost(double[] inputs){//the "Rastrigin function" and it's our cost function we are trying to minimize
    double sum = 0;
    for(int i = 0 ; i < inputs.length ; i++){
        sum += ( Math.pow(inputs[i],2) - ( 10 * Math.cos(2 * Math.PI * inputs[i]) ) );
    }
    return (10 * inputs.length) + sum;
}

private static double [] getRandomInput(){//a random set of inputs to start at
    double [] randomInput = new double[INPUT_DIMENSIONALITY];
    for(int i = 0 ; i < randomInput.length ; i++){
        randomInput[i] = LOWEST_INPUT + (HIGHEST_INPUT - LOWEST_INPUT) * Math.random();
    }
    return randomInput;
}

private static double [] getNeighbor(double [] solution, double currentTemperature){ //we get a neighbor by mutating a number of inputs(previously specified),
                                                                                    //by a mutation factor, we used "Adaptive Step Size" method for neighbor choosing to enhance the nature and flow of SA
    double [] neighbor = solution.clone();//first we set the neighbor to be a copy of the solution
    int [] indexesToBeMutated = getRandomIndexes();//we get indexes by random to be mutated

    for(int i = 0 ; i < NUMBER_OF_INPUTS_TO_MUTATE_FOR_NEIGHBOR ; i++){

        //while mutating the inputs half the times we add to them and half the times we subtract from them
        double random = Math.random();//return a random number between 0 and 1
        if(random >= 0.5) {//we add
            neighbor[indexesToBeMutated[i]] += getMutationMagnitude(currentTemperature);
        }
        else{//we subtract
            neighbor[indexesToBeMutated[i]] -= getMutationMagnitude(currentTemperature);
        }
        if(neighbor[indexesToBeMutated[i]] > 2){//to insure staying in the bounds due to the problem specifications
            neighbor[indexesToBeMutated[i]] = 2;
        }
        if(neighbor[indexesToBeMutated[i]] < -2){//to insure staying in the bounds due to the problem specifications
            neighbor[indexesToBeMutated[i]] = -2;
        }
    }
    return neighbor;
}

private static double getMutationMagnitude(double currentTemperature) {
   return ((currentTemperature / INITIAL_TEMPERATURE) * MUTATION_FACTOR);//we used the ratio of the current temp to the initial temp to be multiplied to the mutation factor to apply the "Adaptive Step Size" method of neighbor choosing
}

private static int[] getRandomIndexes() {//this method returns indexes for the inputs to be mutated
    int [] indexes = new int[NUMBER_OF_INPUTS_TO_MUTATE_FOR_NEIGHBOR];
    int randomIndex = 0;
    for(int i = 0 ; i < indexes.length ; i++){
        if(i == 0){
            indexes[i] = getNewRandomIndex();
        }
        else{//to insure indexes are unique some extra work is below:
            randomIndex = getNewRandomIndex();
            while(randomIndexAlreadyChosen(indexes, randomIndex, i)){
                randomIndex = getNewRandomIndex();
            }//when we exit this while loop we are sure that the new random index is not chosen before
            indexes[i] = randomIndex;
        }
    }
    return indexes;
}
private static int getNewRandomIndex(){
    return (int)Math.floor(INPUT_DIMENSIONALITY * Math.random());
}


private static boolean randomIndexAlreadyChosen(int [] indexes, int randomIndex, int i) {
    boolean existsInArray = false;
    for(int k = 0 ; k < i ; k++){//"i" represents where are we going to place the new unique index, we only search if it exists between the previously filled unique indexes (to save time and to be able to mutate the index 0 ;) )
        if(indexes[k] == randomIndex){//then it already exists in the indexes to be mutated array
            existsInArray = true;
            break;
        }
    }
    return existsInArray;
}

private static double calculateCoolingRateThatProvideACertainDistribution(){//this method calculate the suitable cooling rate to create the wanted exploration vs exploitation ratios
    long numberOfExplorationIterations = (long) Math.ceil(EXPLORATION_PERCENTAGE * ITERATIONS_NUMBER);
    double coolingRate = 0.99;

    int i;
    do{
        i = 0;
        double temp = INITIAL_TEMPERATURE;
        //this loop calculates how many iteration does the cooling rate needs to make Boltzmann probability = 0
        do {
            temp = temp * coolingRate;
            i++;
        } while (Math.exp(-1 / temp) != 0);

        if(i < numberOfExplorationIterations){ //this means that the cooling is so fast,  so we increase the cooling rate
            String str = coolingRate + "";
            String leastSignificantFigure = str.charAt(str.length() - 1) + "";
            int leastSignificantFigureAsInt = Integer.parseInt(leastSignificantFigure);
            str = str.substring(0, str.length() - 1);
            if(leastSignificantFigureAsInt == 9){
                str = str + "91";
            }
            else{
                leastSignificantFigureAsInt++;
                str = str + leastSignificantFigureAsInt;
            }
            coolingRate = Double.parseDouble(str);
        }
        else if(i > numberOfExplorationIterations){ //this means that the cooling is so slow,  so we decrease the cooling rate
            String str = coolingRate + "";
            String leastSignificantFigure = str.charAt(str.length() - 1) + "";
            int leastSignificantFigureAsInt = Integer.parseInt(leastSignificantFigure);
            leastSignificantFigureAsInt--;
            str = str.substring(0, str.length()-1);
            str = str + leastSignificantFigureAsInt + "1";
            coolingRate = Double.parseDouble(str);
        }
        else{
            break;
        }
        String str = coolingRate + "";

    }while(i != numberOfExplorationIterations);
    return coolingRate;
}
}