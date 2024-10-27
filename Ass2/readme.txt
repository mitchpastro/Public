Title -
    Aggregation server with consistency management and RESTful API

Description -
    This application generates a server that can provide clients access to a .json file using 
    sockets over internet. The file is provided and updadet by the content server in the form 
    of a .txt file and uses lamport timeing to ensure consistency.Please be aware that this 
    project is very much unfinnished, it is hoped to add features such as error handeling and 
    fault toleration.

How to run and use -
    1) Make sure all three files (Content server, Aggregation Server and Client)
        are compiled using javac

    2) To run the Aggregation Server, use "java AggregationServer.java" command in terminal
        a) The aggregation server automaticly receives and applys requests
        b) To close the Aggregation server, type "exit" into the terminal

    3) To run a Content Server, use "java ContentServer.java" in separate terminal
        a) To request a PUT, type "PUT" into the terminal
        b) To close a Content Server, type "exit" into the terminal

    4) To run a Client, use "java Client.java" in separate terminal
        a) To request a get, type in "GET"
        b) To close the client, type "Over"
        c) When the client disconnects, it will print out the .json filename if possible.