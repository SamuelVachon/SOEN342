# SOEN342

Samuel Vachon 40281580  
Sergio Abreo Alvarez 40274677  
Hossam Mostafa 40245337  

# Iteration 1
Contributions: All members contributed equally for the delivery of this iteration, the coding was mostly done follwing the "Pair programming" methodologie using Sergio's(pergioa) laptop. The diagrams and operaiton contracts were task evenly distributed aswell.

In order to use the program for the project, the user should navigate to the path : `src/Iteration_1/src` and use the make file provided to compile, run or clean.  
  
  The user has the option to use the follwing commands in the terminal while being at the above specifed path.

  `make compile` to compile the classes but not running the code.  
  `make run` to compile and run the driver class.  
  `make clean` to delete the compiled classes.  

Once the classes have been compiled and are runnig by using the makefile, the user will then be shown the comand line application with a basic UI that allows the user to see all the available cities, and will then be able to use the application by following the given options of quiting or planning a trip.  

Trips are planned with 2 or less connections, the user will be asked to provide the city of departure and arrival. The program will then find all trips for the inputs given and will be able to further sort their options by price for either first or second class and will also be able to sort by travel time.   

The trip duration takes into consideration the amount of time the person would have to wait for their connection train to depart. If the user cannot make it to the follwing train connection due to the previous train arrival time being after the connections departure then the trip duration takes into accound the total hours of wait time till the train for the next day.