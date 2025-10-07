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

Trips are planned with 2 or less connections, the user will be asked to provide the city of departure and arrival, as well as the day of the week he will depart and the type of train he is looking for, he can choose to not filter the results by selecting "all". The program will then find all trips for the inputs given and will be able to further sort their options by price for either first or second class and will also be able to sort by travel time.   

The trip duration takes into consideration the amount of time the person would have to wait for their connection train to depart. If the user cannot make it to the follwing train connection due to the previous train arrival time being after the connections departure then the trip duration takes into accound the total hours of wait time till the train for the next day.


# Iteration 2  
Contributions: All members contributed equally for the delivery of this iteration working using "Pair programming".  

In order to use this iteration of the code the user navigates to the path: `Iteration_2/src` and uses the makefile in that folder to compile run or clean.


  `make compile` to compile the classes but not running the code.  
  `make run` to compile and run the driver class.  
  `make clean` to delete the compiled classes.  

Like for Iteration 1 the same core use cases are still the same, with the addition that on the main menu the user will also be able to view all made bookings with a given session. If the user chooses that option he will be prompted to proved the name and id of the costumer he whishes to see all the bookings for.
when the user plans a trip he will also be able to book a trip from the results that he is given. If he chooses that option he will be prompted the number of bookings needed, the trip that it will be making the bookings for and the costumer's details. The user will provde a name, ID and age for each costumer.
Once the booking is made the user will then be able to see the deatils of the booking wich include the trip information and the costumer's details.

# Iteration 3  
Contributions: All members contributed equally for the delivery of this iteration working using "Pair programming".  

For this iteration of the project we only added the use of a DB for data persistance. All use cases remained unchanged therefore the program behaves just like it did for iteration 2.  

For the DB we used a docker container. To run the program the user will have to navigate to the follwing path `Iteration_3/src` and use the makefile provided. 

on a first run the user will have to run the following set of commands

`make init-db`: this will initialize the DB for the user on a docker container. 

`make start-db`: this will start the docker container of the DB

`make run`: this will run the program if the user has initilized the DB and the container is up and running.  

It is adviced for the user to go over the makefile to look at all the options available to run the program and handle the DB container.

# Iteration 1
Contributions: All members contributed equally for the delivery of this iteration, the coding was mostly done follwing the "Pair programming" methodologie using Sergio's(pergioa) laptop. The diagrams and operaiton contracts were task evenly distributed aswell.

In order to use the program for the project, the user should navigate to the path : `src/Iteration_1/src` and use the make file provided to compile, run or clean.  
  
  The user has the option to use the follwing commands in the terminal while being at the above specifed path.

  `make compile` to compile the classes but not running the code.  
  `make run` to compile and run the driver class.  
  `make clean` to delete the compiled classes.  

Once the classes have been compiled and are runnig by using the makefile, the user will then be shown the comand line application with a basic UI that allows the user to see all the available cities, and will then be able to use the application by following the given options of quiting or planning a trip.  

Trips are planned with 2 or less connections, the user will be asked to provide the city of departure and arrival, as well as the day of the week he will depart and the type of train he is looking for, he can choose to not filter the results by selecting "all". The program will then find all trips for the inputs given and will be able to further sort their options by price for either first or second class and will also be able to sort by travel time.   

The trip duration takes into consideration the amount of time the person would have to wait for their connection train to depart. If the user cannot make it to the follwing train connection due to the previous train arrival time being after the connections departure then the trip duration takes into accound the total hours of wait time till the train for the next day.


# Iteration 2  
Contributions: All members contributed equally for the delivery of this iteration working using "Pair programming".  

In order to use this iteration of the code the user navigates to the path: `Iteration_2/src` and uses the makefile in that folder to compile run or clean.


  `make compile` to compile the classes but not running the code.  
  `make run` to compile and run the driver class.  
  `make clean` to delete the compiled classes.  

Like for Iteration 1 the same core use cases are still the same, with the addition that on the main menu the user will also be able to view all made bookings with a given session. If the user chooses that option he will be prompted to proved the name and id of the costumer he whishes to see all the bookings for.
when the user plans a trip he will also be able to book a trip from the results that he is given. If he chooses that option he will be prompted the number of bookings needed, the trip that it will be making the bookings for and the costumer's details. The user will provde a name, ID and age for each costumer.
Once the booking is made the user will then be able to see the deatils of the booking wich include the trip information and the costumer's details.

# Iteration 3  
Contributions: All members contributed equally for the delivery of this iteration working using "Pair programming".  

For this iteration of the project we only added the use of a DB for data persistance. All use cases remained unchanged therefore the program behaves just like it did for iteration 2.  

For the DB we used a docker container. To run the program the user will have to navigate to the follwing path `Iteration_3/src` and use the makefile provided. 

on a first run the user will have to run the following set of commands

`make init-db`: this will initialize the DB for the user on a docker container. 

`make start-db`: this will start the docker container of the DB

`make run`: this will run the program if the user has initilized the DB and the container is up and running.  

It is adviced for the user to go over the makefile to look at all the options available to run the program and handle the DB container.

# Iteration 1
In order to use the program for the project, the user should navigate to the path : `src/Iteration_1/src` and use the make file provided to compile, run or clean.  
  
  The user has the option to use the follwing commands in the terminal while being at the above specifed path.

  `make compile` to compile the classes but not running the code.  
  `make run` to compile and run the driver class.  
  `make clean` to delete the compiled classes.  

Once the classes have been compiled and are runnig by using the makefile, the user will then be shown the comand line application with a basic UI that allows the user to see all the available cities, and will then be able to use the application by following the given options of quiting or planning a trip.  

Trips are planned with 2 or less connections, the user will be asked to provide the city of departure and arrival. The program will then find all trips for the inputs given and will be able to further sort their options by price for either first or second class and will also be able to sort by travel time.   

The trip duration takes into consideration the amount of time the person would have to wait for their connection train to depart. If the user cannot make it to the follwing train connection due to the previous train arrival time being after the connections departure then the trip duration takes into accound the total hours of wait time till the train for the next day.