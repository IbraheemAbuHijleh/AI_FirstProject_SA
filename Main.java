package application;
	
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class Main extends Application {
    //Problem specification:
    public static final int INPUT_DIMENSIONALITY = 15;
    public static final int LOWEST_INPUT = -2;
    public static final int HIGHEST_INPUT = 2;
    /****************************************************/

    //Simulated Annealing parameters:
    public static final double INITIAL_TEMPERATURE = 1000;//it can be any different positive value since we are adjusting the cooling rate to suite it anyway
    public static final int ITERATIONS_NUMBER = 1000; //increase for better results but longer processing time ;)
    public static final double EXPLORATION_PERCENTAGE = 0.8;//the need of this variable is demonstrated below
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
    public static final int NUMBER_OF_INPUTS_TO_MUTATE_FOR_NEIGHBOR = 15;
    public static final double MUTATION_FACTOR = 2;
    /*****************************************************************************************************************/

    private static SimulatedAnnealingResult simulatedAnnealing(){

        double temperature = INITIAL_TEMPERATURE;

        //At first, we create a random set of inputs and choose it to be the solution.
        double [] solution = getRandomInput();
        //we calculate it's cost
        double solutionCost = calculateCost(solution);

        double [] iterationsResults = new double[ITERATIONS_NUMBER];//here we will store the results of every iteration to diagram the progress
        //the following 3 lines are for debugging
        //double [] temperatureResults = new double[ITERATIONS_NUMBER];
        //double [] boltzmannProbabilities = new double[ITERATIONS_NUMBER];
        //double [] systemEnergies = new double[ITERATIONS_NUMBER];

        for(int i = 0 ; i < ITERATIONS_NUMBER ; i++){
            //we choose a neighbor of our solution
            double [] neighborSolution = getNeighbor(solution, temperature);
            //we calculate the neighbor's cost
            double neighborCost = calculateCost(neighborSolution);

            //the following 3 lines are for debugging
            //temperatureResults[i] = temperature;
            //systemEnergies[i] = -Math.abs(solutionCost - neighborCost);
            //boltzmannProbabilities[i] = Math.exp( (-Math.abs(solutionCost - neighborCost) -10 ) / temperature );

            if(neighborCost <= solutionCost){//if the neighbor has less cost we accept it as our new solution
                solution = neighborSolution;
                solutionCost = neighborCost;
            }
            else{//if the neighbor have Greater cost we accept it by "boltzmann probability"
                double boltzmannProbability = Math.exp( (-Math.abs(solutionCost - neighborCost) ) / temperature );

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

        //the following 3 lines are for debugging
        //SimulatedAnnealingResult simulatedAnnealingResult = new SimulatedAnnealingResult(temperatureResults, solutionCost);
        //SimulatedAnnealingResult simulatedAnnealingResult = new SimulatedAnnealingResult(systemEnergies, solutionCost);
        //SimulatedAnnealingResult simulatedAnnealingResult = new SimulatedAnnealingResult(boltzmannProbabilities, solutionCost);
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
        return ( ((currentTemperature / INITIAL_TEMPERATURE ) * MUTATION_FACTOR) + (0.5 * MUTATION_FACTOR));//we used the ratio of the current temp to the initial temp to be multiplied to the mutation factor to apply the "Adaptive Step Size" method of neighbor choosing
    }

    private static int[] getRandomIndexes() {//this method returns indexes for the inputs to be mutated
        int [] indexes = new int[NUMBER_OF_INPUTS_TO_MUTATE_FOR_NEIGHBOR];
        if(NUMBER_OF_INPUTS_TO_MUTATE_FOR_NEIGHBOR == INPUT_DIMENSIONALITY){
            indexes = new int[]{0,1,2,3,4,5,6,7,8,9,10,11,12,13,14};
        }
        else {
            int randomIndex = 0;
            for (int i = 0; i < indexes.length; i++) {
                if (i == 0) {
                    indexes[i] = getNewRandomIndex();
                } else {//to insure indexes are unique some extra work is below:
                    randomIndex = getNewRandomIndex();
                    while (randomIndexAlreadyChosen(indexes, randomIndex, i)) {
                        randomIndex = getNewRandomIndex();
                    }//when we exit this while loop we are sure that the new random index is not chosen before
                    indexes[i] = randomIndex;
                }
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
        double coolingRate = 0.9;

        int i;
        do{
            i = 0;
            double temp = INITIAL_TEMPERATURE;
            //this loop calculates how many iteration does the cooling rate needs to make Boltzmann probability = 0
            do {
                temp = temp * coolingRate;
                i++;
            } while (Math.exp(-100 / temp) != 0);

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
        System.out.println("hello");
        return coolingRate;
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("SIMULATED ANNEALING");

        // X and Y axes
        NumberAxis xAxis = new NumberAxis(0, ITERATIONS_NUMBER, ITERATIONS_NUMBER/10);
        xAxis.setLabel("Iterations");
        xAxis.lookup(".axis-label").setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        xAxis.setTickLabelFont(Font.font("Arial", FontWeight.BOLD, 12));

        NumberAxis yAxis = new NumberAxis(0, 300, 10); //cost
        //the following 3 lines are for debugging
        //NumberAxis yAxis = new NumberAxis(0, 1000, 33); //temp
        //NumberAxis yAxis = new NumberAxis(-30, 0, 1); //energy
        //NumberAxis yAxis = new NumberAxis(0, 1, 0.1); //boltzmann

        yAxis.setLabel("function value (Cost)");
        yAxis.lookup(".axis-label").setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        yAxis.setTickLabelFont(Font.font("Arial", FontWeight.BOLD, 12));


        LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Cost Over Iterations");
        lineChart.lookup(".chart-title").setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: black;");
        lineChart.setCreateSymbols(false);
        lineChart.setLegendVisible(false);

        /** HERE IS THE SIMULATED ANNEALING FUNCTION CALL **/
        SimulatedAnnealingResult result = simulatedAnnealing();
        double[] values = result.getIterationsResults();


        ObservableList<XYChart.Data<Number, Number>> dataPoints = FXCollections.observableArrayList();
        for (int i = 0; i < values.length; i++) {
            double val = values[i];
            if (!Double.isNaN(val) && !Double.isInfinite(val)) {
                dataPoints.add(new XYChart.Data<>(i, val));
            }
        }

        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName("Cost");
        series.setData(dataPoints);

        lineChart.getData().add(series);

        Label label = new Label("Final Cost: ");
        label.setStyle("-fx-font: bold 16pt 'Arial'; -fx-text-fill: black;");

        TextField textField = new TextField();
        textField.setEditable(false);
        textField.setText(String.valueOf(result.getFinalResult()).substring(0,7));
        textField.setStyle("-fx-font: bold 14pt Arial;");
        textField.setMaxWidth(120);
        textField.setFocusTraversable(false);

        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER);
        hBox.getChildren().addAll(label,textField);

        BorderPane root = new BorderPane();
        root.setCenter(lineChart);
        root.setBottom(hBox);
        root.setStyle("-fx-background-color: #b6cafa;");

        Scene scene = new Scene(root, 1400, 750);

        scene.setFill(Color.rgb(90, 109, 156));
        stage.setScene(scene);
        Image image = new Image("file:///C:/Users/hp/Downloads/ibraheem.jpeg");
        stage.getIcons().add(image);
        stage.setMaximized(true);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}