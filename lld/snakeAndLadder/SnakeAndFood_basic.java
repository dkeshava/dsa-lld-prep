import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

class GameBoard{
    private int width;
    private int height;
    private static GameBoard instance;
    private GameBoard(int width,int height){
        this.height=height;
        this.width=width;
    }
    public static GameBoard getInstance(int width,int height){
        if(instance==null){
            instance=new GameBoard(width, height);
        }
        return instance;
    }
    //getters
    public int getWidth(){return width;}
    public int getHeight(){return height;}
}

abstract class FoodItem{
    protected int row;
    protected int column;
    protected int points;
    public FoodItem(int row, int col){
        this.row=row;
        this.column=col;
    }
    public int getRow(){return row;}
    public int getColumn(){return column;}
    public int getPoints(){return points;}
}

class NormalFood extends FoodItem{
    public NormalFood(int row,int col){
        super(row, col);
        this.points=2;
    }
}

class EnergyFood extends FoodItem{
    public EnergyFood(int row, int col){
        super(row, col);
        this.points=5;
    }
}
class Pair{
    private int row, col;
    public Pair(int row, int col){
        this.col=col;
        this.row=row;
    }
    public int getRow(){return row;}
    public int getColumn(){return col;}
    @Override
    public boolean equals(Object o){
        if(this==o) return true;
        if(!(o instanceof Pair)) return false;
        Pair pair=(Pair) o;
        return row==pair.row && col==pair.col;
    }
    @Override
    public int hashCode(){
        return Objects.hash(row,col);
    }
}
interface MovementStrategy{
    Pair getNextPosition(Pair currentHead, String direction);
}

class HumanMovementStrategy implements MovementStrategy{
    @Override
    public Pair getNextPosition(Pair currentHead, String direction){
        int row=currentHead.getRow();
        int col=currentHead.getColumn();
        return switch (direction) {
            case "U" -> new Pair(row-1, col);
            case "D" -> new Pair(row+1, col);
            case "L" -> new Pair(row, col-1);
            case "R" -> new Pair(row, col+1);
            default -> currentHead;
        };
    }
}
// class computerMovementStrategy implements MovementStrategy{
//     @Override
//     // public Pair getNextPosition(Pair currentHead, String direction){
        
//     // }
// }
//game controller
class SnakeGame{
    private GameBoard gameBoard;
    private Deque<Pair> snake;
    private Set<Pair> snakeBody;
    private MovementStrategy movementStrategy;
    private FoodItem currentFood;
    private String direction;
    private int score;
    private boolean gameOver;

    public SnakeGame(int width,int height){
        this.gameBoard=GameBoard.getInstance(width, height);
        this.movementStrategy=new HumanMovementStrategy();
        this.snake=new LinkedList<>();
        this.snakeBody=new HashSet<>();
        initializeGame();
    }
    private void initializeGame(){
        //place snake at center
        int centerRow=gameBoard.getHeight()/2;
        int centerColumn=gameBoard.getWidth()/2;
        Pair start = new Pair(centerRow, centerColumn);
        snake.offerFirst(start);
        snakeBody.add(start);
        this.direction="R";
        score=0;
        gameOver=false;
        spawnFood();
    }
    private void spawnFood(){
        int rows=gameBoard.getHeight();
        int cols=gameBoard.getWidth();
        Random rand=new Random();
        while(true){
            int row=rand.nextInt(rows);
            int col=rand.nextInt(cols);
            Pair pos=new Pair(row, col);
            if(!snakeBody.contains(pos)){
                currentFood=rand.nextBoolean()? 
                new NormalFood(row,col) : new EnergyFood(row, col);
                break;
            }
        }
    }
    public void move(){
        Pair nextHead=movementStrategy.getNextPosition(snake.peekFirst(), direction);
        boolean isWallCollision=false,isSelfCollision=false;
        if(nextHead.getRow()<0 || nextHead.getRow()>=gameBoard.getHeight() || nextHead.getColumn()<0 || nextHead.getColumn()>=gameBoard.getWidth()) isWallCollision=true;
        if(snakeBody.contains(nextHead)) isSelfCollision=true;
        if(isWallCollision || isSelfCollision){
            gameOver=true;
            System.out.println("Game Over!");
            System.out.println("Your Final Score: " + score);
            return;
        }
        if(nextHead.getRow()==currentFood.getRow() && nextHead.getColumn()==currentFood.getColumn()){
            score+=currentFood.getPoints();
            snake.addFirst(nextHead);
            snakeBody.add(nextHead);
            spawnFood();
            return;
        }
        else{
            Pair tail = snake.removeLast();
            snakeBody.remove(tail);

            snake.addFirst(nextHead);
            snakeBody.add(nextHead);
        }
    }
    public void changeDirection(String newDirection){
        if(newDirection == null || newDirection.isEmpty()) return;
        if((direction.equals("U") && newDirection.equals("D")) || 
            (direction.equals("D") && newDirection.equals("U")) ||
            (direction.equals("L") && newDirection.equals("R")) ||
            (direction.equals("R") && newDirection.equals("L")))
                return;
        //also can give values to direction and if sum becomes zero then don't change direction
        this.direction=newDirection;
    }
    public void printBoard(){
        for(int i=0;i<gameBoard.getHeight();i++){
            System.out.print("| ");
            for(int j=0;j<gameBoard.getWidth();j++){
                if(currentFood.getRow()==i && currentFood.getColumn()==j) System.out.print("f | ");
                else if(snakeBody.contains(new Pair(i, j))) System.out.print("s | ");
                else System.out.print(". | ");
            }
            System.out.println();
        }
    }
    public void start(){
        Scanner sc = new Scanner(System.in);
        while(!gameOver){
            printBoard();
            System.out.print("Enter direction (U/D/L/R): ");
            String input = sc.nextLine().trim().toUpperCase();
            changeDirection(input);
            move();
        }
    }
}

public class SnakeAndFood_basic{
    public static void main(String[] args) {
        SnakeGame snakeGame=new SnakeGame(6,6);
        snakeGame.start();
    }
}
