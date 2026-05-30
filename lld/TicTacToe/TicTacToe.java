import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

enum GameStatus{
    IN_PROGRESS,
    WIN,
    DRAW
}

// class Pair{
//     private int row,col;
//     public Pair(int row,int col){
//         this.row=row;
//         this.col=col;
//     }
//     public int getRow(){return row;}
//     public int getColumn(){return col;}
//     @Override
//     public boolean equals(Object o){
//         if(this==o) return true;
//         if(!(o instanceof Pair)) return false;
//         Pair pair=(Pair) o;
//         return row==pair.row && col==pair.col;
//     }
//     public int hashCode(){
//         return Objects.hash(row,col);
//     }
// }

class GameBoard{
    private int size; //n*n 
    private char[][] grid;
    //private static GameBoard instance;
    public GameBoard(int size){
        this.size=size;
        grid=new char[size][size];
    }
    // public static GameBoard getInstance(int size){
    //     if(instance==null){
    //         instance=new GameBoard(size);
    //     }
    //     return instance;
    // }
    public int getSize(){return size;}
    public char[][] getGrid(){return grid;}
    public boolean isValidMove(int row, int col){
        if(row < 0 || row >= size ||
        col < 0 || col >= size){
            return false;
        }
        return grid[row][col]=='\0';
    }
    public void placeMove(int row, int col, char symbol){
        grid[row][col]=symbol;
    }
    public void printBoard(){
        for (int i = 0; i < grid.length; i++) {
            System.out.print("| ");
            for (int j = 0; j < grid[i].length; j++) {
                if (grid[i][j] == '\0') {
                    System.out.print(". | ");
                } else {
                    System.out.print(grid[i][j] + " | ");
                }
            }
            System.out.println();
        }
    }
}

class Player{
    private String id;
    private String name;
    private char symbol;
    public Player(String id, String name, char symbol){
        this.id=id;
        this.name=name;
        this.symbol=symbol;
    }
    public char getSymbol(){return symbol;}
    public String getName(){return name;}
}

class TicTacToeController{
    private GameBoard gameBoard; 
    private List<Player> players;
    private int currentPlayerIndex;
    private GameStatus gameStatus;
    private boolean gameOver;
    private int movesPlayed;
    public TicTacToeController(int size){
        gameBoard=new GameBoard(size);
        this.players=new ArrayList<>();
        this.currentPlayerIndex=0;
        this.movesPlayed=0;
        this.gameStatus=GameStatus.IN_PROGRESS;
        gameOver=false;
    }
    public void initializePlayers(List<String> names){
        List<Character> symbols = List.of('X','O','A','B','C');
        if(names.size() > symbols.size()){
            throw new IllegalArgumentException("Too many players");
        }
        for(int i=0;i<names.size();i++){
            players.add(
                new Player(
                    String.valueOf(i+1),
                    names.get(i),
                    symbols.get(i)
                )
            );
        }
    }
    public void start(){
        Scanner sc = new Scanner(System.in);
        while(!gameOver){
            gameBoard.printBoard();
            System.out.print("P"+(currentPlayerIndex+1)+": Enter your location to place (Row and Column): ");
            int row = sc.nextInt();
            int col = sc.nextInt();
            makeMove(row,col);
        }
    }
    
    public void switchTurn(){
        if(currentPlayerIndex==players.size()-1) currentPlayerIndex=0;
        else currentPlayerIndex++;
    }
    public void checkGameStatus(int row,int col){
        char[][] grid=gameBoard.getGrid();
        int n=gameBoard.getSize();
        char symbol=grid[row][col];

        boolean rowWin=true,colWin=true,diagonalWin=true,antiDiagonalWin=true;

        for(int i=0;i<n;i++){
            if(grid[row][i]!=symbol) rowWin=false;
            if(grid[i][col]!=symbol) colWin=false;
        }

        //diagonal win
        if(row==col){
            for(int i=0;i<n;i++){
                if(grid[i][i]!=symbol) {
                    diagonalWin=false;
                    break;
                }
            }
        }else {
            diagonalWin = false;
        }

        if(row+col==n-1){
            for(int i=0;i<n;i++){
                if(grid[i][n-1-i]!=symbol){
                    antiDiagonalWin=false;
                    break;
                }
            }
        }else {
            antiDiagonalWin = false;
        }

        if(rowWin || colWin || diagonalWin || antiDiagonalWin){
            gameStatus=GameStatus.WIN;
            gameOver=true;
            System.out.println("Player "+players.get(currentPlayerIndex).getName() +" wins!");
            return;
        }
        //draw check
        if(movesPlayed == gameBoard.getSize() * gameBoard.getSize()){
            gameStatus = GameStatus.DRAW;
            gameOver = true;
            System.out.println("Game Draw!");
        }
        //else gameStatus=GameStatus.InProgress;
    }
    public void makeMove(int row,int col){
        //check if move is valid
        if(!gameBoard.isValidMove(row,col)){
            System.out.println("Invalid move!");
            return;
        }
        Player currentPlayer=players.get(currentPlayerIndex);
        char symbol=currentPlayer.getSymbol();
        gameBoard.placeMove(row, col, symbol);
        //check if win or draw and update
        movesPlayed++;
        checkGameStatus(row,col);
        if(!gameOver){
            switchTurn();
        }
    }
}

public class TicTacToe {
    public static void main(String[] args) {
        // private static final List<Character> availableSymbols=List.of('X', 'O', 'A', 'B', 'C', 'D', 'E'); 
        // Player p1=new Player("1", "dk", 'X');
        // Player p2=new Player("2", "md", 'O');
        // List<Player> players=List.of(p1,p2);
        List<String> names=List.of("dk","md"); 
        TicTacToeController tictactoe=new TicTacToeController(3);
        tictactoe.initializePlayers(names);
        //tictactoe.printBoard();
        tictactoe.start();
    }
}
