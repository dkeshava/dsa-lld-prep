
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.TreeSet;

enum Direction{
    UP,DOWN, IDLE
}
enum ElevatorState{
    IDLE,
    MOVING,
    STOPPED,
    MAINTAINANCE
}
// class ElevatorRequest{

// }
class ExternalButton{ //should create factory pattern for creating external buttons for each floor
    int floorNumber;
    ElevatorController controller;
    public ExternalButton(int floorNumber, ElevatorController controller){
        this.floorNumber=floorNumber;
        this.controller=controller;
    }
    public void pressButton(Direction direction){
        ExternalRequest request=new ExternalRequest(floorNumber, direction);
        controller.submitRequest(request);
    }
}
class ExternalRequest{
    private int sourceFloor;
    private Direction direction;
    public ExternalRequest(int sourceFloor,Direction direction){
        this.sourceFloor=sourceFloor;
        this.direction=direction;
    }
    public int getSourceFloor(){
        return sourceFloor;
    }
    public Direction getDirection(){
        return direction;
    }
}
class InternalButton{
    Elevator elevator;
    public InternalButton(Elevator elevator){
        this.elevator=elevator;
    }
    public void pressButton(int destinationFloor){
        InternalRequest request=new InternalRequest(destinationFloor);
        elevator.addInternalRequest(request);
    }
}
class InternalRequest{
    private int destinationFloor;
    public InternalRequest(int destinationFloor){
        this.destinationFloor=destinationFloor;
    }
    public int getDestinationFloor(){
        return destinationFloor;
    }
}
class Elevator{
    private int elevatorId;
    //List<InternalButton> internalButtons; //person chooses one button in this specific lift (I've added this because maybe different types of elevators have different buttons, as some can't go to all floors)
    //Queue<InternalRequest> internalRequests;
    TreeSet<Integer> upRequests;
    TreeSet<Integer> downRequests;
    private int currentFloor;
    private Direction direction;
    private ElevatorState elevatorState;
    public Elevator(int elevatorId){
        this.elevatorId=elevatorId;
        this.upRequests=new TreeSet<>();
        this.downRequests=new TreeSet<>();
        this.currentFloor=0;
        this.direction=Direction.IDLE;
        this.elevatorState=ElevatorState.IDLE; 
    }
    // public void moveToFloor(int destinationFloor){
    //     System.out.println("Elevator "+elevatorId+" moving to floor: "+destinationFloor);
    //     if(destinationFloor>currentFloor) {
    //         direction=Direction.UP;
    //         elevatorState=ElevatorState.MOVING;
    //         while(currentFloor!=destinationFloor){
    //             currentFloor++;
    //             System.out.println("Elevator is at floor: "+currentFloor);
    //         }
    //     }
    //     else if(currentFloor>destinationFloor) {
    //         direction=Direction.DOWN;
    //         elevatorState=ElevatorState.MOVING;
    //         while(currentFloor!=destinationFloor){
    //             currentFloor--;
    //             System.out.println("Elevator is at floor: "+currentFloor);
    //         }
    //     }
    //     else System.out.println("Lift is idle");
    //     System.out.println("Destination arrived");
    //     direction=Direction.IDLE;
    //     elevatorState=ElevatorState.IDLE;
    // }
    public void addInternalRequest(InternalRequest request){
        int destinationFloor=request.getDestinationFloor();
        if(destinationFloor>currentFloor) upRequests.add(destinationFloor);
        else if(destinationFloor<currentFloor) downRequests.add(destinationFloor);
    }
    public void addExternalRequest(ExternalRequest externalRequest){
        int destinationFloor=externalRequest.getSourceFloor();
        if(destinationFloor>currentFloor) upRequests.add(destinationFloor);
        else if(destinationFloor<currentFloor) downRequests.add(destinationFloor);
    }
    // public void processRequests(){
    //     // while(!upRequests.isEmpty() || !downRequests.isEmpty()){
    //     //     if(direction==Direction.IDLE){
    //     //         if(!upRequests.isEmpty()) {
    //     //             direction=Direction.UP;
    //     //             processUpRequests();
    //     //         }
    //     //         else{
    //     //             direction=Direction.DOWN;
    //     //             processDownRequests();
    //     //         }
    //     //     }
    //     //     else if(direction==Direction.UP) {
    //     //         processUpRequests();
    //     //         if(!downRequests.isEmpty()) direction=Direction.DOWN;
    //     //     }
    //     //     else{
    //     //         processDownRequests();
    //     //         if(!upRequests.isEmpty()) direction=Direction.UP;
    //     //     }
    //     // }
    //     // direction=Direction.IDLE;
    //     while(true){
    //         Integer nextStop=getNextStop();
    //         if(nextStop==null) {
    //             direction=Direction.IDLE;
    //             break;
    //         };
    //         moveToFloor(nextStop);
    //         upRequests.remove(nextStop);
    //         downRequests.remove(nextStop);
    //     }
    // }
    // private Integer getNextStop(){
    //     if(direction==Direction.UP){
    //         Integer next=upRequests.ceiling(currentFloor+1);
    //         if(next!=null) return next;
    //         direction=Direction.DOWN;
    //         return downRequests.ceiling(currentFloor-1);
    //     }
    //     else if(direction==Direction.DOWN){
    //         Integer next=downRequests.ceiling(currentFloor-1);
    //         if(next!=null) return next;
    //         direction=Direction.UP;
    //         return downRequests.ceiling(currentFloor+1);
    //     }
    //     else{
    //         if(!upRequests.isEmpty()){
    //             direction=Direction.UP;
    //             return upRequests.ceiling(currentFloor+1);
    //         }
    //         else if(!downRequests.isEmpty()){
    //             direction = Direction.DOWN;
    //             return downRequests.floor(currentFloor - 1);
    //         }
    //     }
    //     return null;
    // }
    // public void processUpRequests(){
    //     while (!upRequests.isEmpty()) {
    //         direction=Direction.UP;
    //         int upRequest=upRequests.pollFirst();
    //         moveToFloor(upRequest);
    //     }
    // }
    // public void processDownRequests(){
    //     while (!downRequests.isEmpty()) {
    //         direction=Direction.DOWN;
    //         int downRequest=downRequests.pollLast();
    //         moveToFloor(downRequest);
    //     }
    // }
    public void step(){
        updateDirection();
        if(direction==Direction.IDLE){
            elevatorState=ElevatorState.IDLE;
            return;
        }
        elevatorState=ElevatorState.MOVING;
        moveOneFloor();
        System.out.println("Elevator "+elevatorId+" is at floor: "+currentFloor);
        handleStopIfNeeded();
    }
    public void moveOneFloor(){
        if(direction==Direction.UP){
            currentFloor++;
        }
        else if (direction==Direction.DOWN){
            currentFloor--;
        }
    }
    public boolean shouldStop(){
        if(direction==Direction.UP){
            return upRequests.contains(currentFloor);   
        }
        else if(direction==Direction.DOWN){
            return downRequests.contains(currentFloor);
        }
        return false;
    }
    public void handleStopIfNeeded(){
        if(!shouldStop()) return;
        System.out.println("Elevator "+elevatorId+" stopping at floor: "+currentFloor);
        elevatorState=ElevatorState.STOPPED;
        //Thread.sleep(1000);
        if(direction==Direction.DOWN){
            downRequests.remove(currentFloor);
        }
        else upRequests.remove(currentFloor);
        elevatorState=ElevatorState.MOVING;
    }
    public void updateDirection(){
        if(direction==Direction.UP){
            Integer next=upRequests.ceiling(currentFloor+1);
            if(next!=null) return;
            if(!downRequests.isEmpty()){
                direction=Direction.DOWN;
                return;
            }
        }
        else if(direction==Direction.DOWN){
            Integer next=downRequests.floor(currentFloor-1);
            if(next!=null) return;
            if(!upRequests.isEmpty()){
                direction=Direction.UP;
                return;
            }
        }
        else{
            if(!upRequests.isEmpty()){
                direction=Direction.UP;
                return;
            }
            if(!downRequests.isEmpty()){
                direction=Direction.DOWN;
                return;
            }
        }
        direction=Direction.IDLE;
    }
    public ElevatorState getElevatorState(){
        return elevatorState;
    }
    public int getCurrentFloor(){
        return currentFloor;
    }
    public Direction getDirection(){
        return direction;
    }
}
// class Floor{
//     private int floorNumber;

// }
// class Building{
//     private String name;
//     List<Elevator> elevators;
//     private ElevatorController elevatorController;

// }
class ElevatorController{
    Queue<ExternalRequest> externalRequests;
    List<Elevator> elevators;
    ElevatorFindingAlgo elevatorFindingAlgo;
    public ElevatorController(List<Elevator> elevators,ElevatorFindingAlgo elevatorFindingAlgo){
        this.externalRequests=new LinkedList<>();
        this.elevators=elevators;
        this.elevatorFindingAlgo=elevatorFindingAlgo;
    }
    //next step is to assign a lift
    public void submitRequest(ExternalRequest request){
        Elevator pickupElevator=elevatorFindingAlgo.findBestElevator(request);
        // if(pickupElevator==null){
        //     System.out.println("No elevator is idle at present, please try after some time");
        //     return;
        // }
        pickupElevator.addExternalRequest(request);
    }
    public Elevator findIdleElevator(List<Elevator> elevators){
        for(Elevator elevator: elevators){
            if(elevator.getElevatorState()==ElevatorState.IDLE) return elevator;
        }
        return elevators.get(0);
    }
}
interface ElevatorFindingAlgo{
    public Elevator findBestElevator(ExternalRequest request);
}
class NearestElevatorAlgo implements ElevatorFindingAlgo{
    List<Elevator> elevators;
    public NearestElevatorAlgo(List<Elevator> elevators){
        this.elevators=elevators;
    }
    @Override
    public Elevator findBestElevator(ExternalRequest request){
        int nearestDistance=Integer.MAX_VALUE;
        Elevator bestElevator=null;
        for(Elevator elevator: elevators){
            if(elevator.getElevatorState()==ElevatorState.IDLE){
                if(Math.abs(elevator.getCurrentFloor()-request.getSourceFloor())<nearestDistance){
                    bestElevator=elevator;
                    nearestDistance=Math.abs(elevator.getCurrentFloor()-request.getSourceFloor());
                }
            }
        }
        if(bestElevator!=null) return bestElevator;
        return elevators.get(0);
    }
}
class SameDirectionAlgo implements ElevatorFindingAlgo{
    List<Elevator> elevators;
    public SameDirectionAlgo(List<Elevator> elevators){
        this.elevators=elevators;
    }
    @Override 
    public Elevator findBestElevator(ExternalRequest request){
        Elevator bestElevator=null;
        Integer minDist=Integer.MAX_VALUE;
        for(Elevator elevator: elevators){
            if(elevator.getDirection()==request.getDirection()){
                if(elevator.getDirection()==Direction.UP && elevator.getCurrentFloor()<=request.getSourceFloor()){
                    int dist=Math.abs(elevator.getCurrentFloor()-request.getSourceFloor());
                    if(dist<minDist){
                        minDist=dist;
                        bestElevator=elevator;
                    }
                }
                else if(elevator.getDirection()==Direction.DOWN && request.getSourceFloor()<=elevator.getCurrentFloor()){
                    int dist=Math.abs(elevator.getCurrentFloor()-request.getSourceFloor());
                    if(dist<minDist){
                        minDist=dist;
                        bestElevator=elevator;
                    }
                }
            }
        }
        if(bestElevator==null){
            for(Elevator elevator: elevators){
                if(elevator.getDirection()==Direction.IDLE){
                    int dist=Math.abs(elevator.getCurrentFloor()-request.getSourceFloor());
                    if(dist<minDist){
                        minDist=dist;
                        bestElevator=elevator;
                    }
                }
            }
        }
        if(bestElevator==null){
            return elevators.get(0);
        }
        return bestElevator;
    }
}
// interface SchedulingStrategy{
    
// }
// class FCFS_strategy implements SchedulingStrategy{}
// class lookSchedulingStrategy implements SchedulingStrategy{}
// class ScanSchedulingStrategy implements SchedulingStrategy{}


public class ElevatorDesignBasic{
    public static void main(String[] args) {
        Elevator e1=new Elevator(1);
        Elevator e2=new Elevator(2);
        // e1.moveToFloor(5);
        // e1.moveToFloor(3);
        List<Elevator> elevators=new ArrayList<>();
        elevators.addAll(Arrays.asList(e1,e2));
        ElevatorFindingAlgo elevatorFindingAlgo=new SameDirectionAlgo(elevators);
        ElevatorController controller=new ElevatorController(elevators,elevatorFindingAlgo);
        ExternalButton extButtonFloor9=new ExternalButton(9, controller);
        extButtonFloor9.pressButton(Direction.DOWN);
        InternalButton buttonBoard1=new InternalButton(e1);
        InternalButton buttonBoard2=new InternalButton(e2);
        buttonBoard1.pressButton(5);
        buttonBoard1.pressButton(3);
        buttonBoard1.pressButton(4);
        buttonBoard1.pressButton(11);
        //e1.processRequests();
        int time = 0;

        while(true){

            System.out.println("TIME : " + time);

            e1.step();
            e2.step();

            if(time == 3){
                controller.submitRequest(
                    new ExternalRequest(6, Direction.UP)
                );
            }
            if(time == 13){
                controller.submitRequest(
                    new ExternalRequest(3, Direction.DOWN)
                );
            }

            time++;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}