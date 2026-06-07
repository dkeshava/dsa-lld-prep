enum PieceType{
    KING,
    QUEEN,
    ROOK,
    BISHOP,
    KNIGHT,
    PAWN
}

enum Color{
    WHITE,
    BLACK
}

enum GameStatus{
    IN_PROGRESS,
    WHITE_WIN,
    BLACK_WIN,
    DRAW
}

class Position{
    private int row;
    private int col;
    public Position(int row,int col){
        this.row=row;
        this.col=col;
    }
    public int getRow(){return row;}
    public int getCol(){return col;}
}

abstract class Piece{
    protected PieceType pieceType;
    protected Color color;

    public Piece(PieceType pieceType, Color color) {
        this.pieceType = pieceType;
        this.color = color;
    }
    public Color getColor(){return color;}
    public abstract boolean canMove(Board board, Position source, Position destination);
}
class King extends Piece{
    public King(PieceType pieceType,Color color){
        super(pieceType,color);
    }
    @Override
    public boolean canMove(Board board, Position source, Position destination){
        int dr=Math.abs(source.getRow()-destination.getRow());
        int dc=Math.abs(source.getCol()-destination.getCol());
        return ((dc==0 && dr==1) || (dr==0 && dc==1)) || (dc==1 && dr==1);
    }
}
class Queen extends Piece{
    public Queen(PieceType pieceType,Color color){
        super(pieceType,color);
    }
    @Override
    public boolean canMove(Board board, Position source, Position destination){
        int dr=Math.abs(source.getRow()-destination.getRow());
        int dc=Math.abs(source.getCol()-destination.getCol());
        return (dr==dc) || ((dr==0) || (dc==0));
    }
}
class Rook extends Piece{
    public Rook(PieceType pieceType,Color color){
        super(pieceType,color);
    }
    @Override
    public boolean canMove(Board board, Position source, Position destination){
        int dr=Math.abs(source.getRow()-destination.getRow());
        int dc=Math.abs(source.getCol()-destination.getCol());
        return (dr==0 || dc==0);
    }
}
class Bishop extends Piece{
    public Bishop(PieceType pieceType,Color color){
        super(pieceType,color);
    }
    @Override
    public boolean canMove(Board board, Position source, Position destination){
        int dr=Math.abs(source.getRow()-destination.getRow());
        int dc=Math.abs(source.getCol()-destination.getCol());
        return (dr==dc);
    }
}
class Knight extends Piece{
    public Knight(PieceType pieceType,Color color){
        super(pieceType,color);
    }
    @Override
    public boolean canMove(Board board, Position source, Position destination){
        int dr=Math.abs(source.getRow()-destination.getRow());
        int dc=Math.abs(source.getCol()-destination.getCol());
        if((dr==2 && dc==1) || (dr==1 && dc==2)) return true;
        return false;
    }
}
class Pawn extends Piece{
    public Pawn(PieceType pieceType,Color color){
        super(pieceType,color);
    }
    @Override
    public boolean canMove(Board board, Position source, Position destination){
        //assuming white is below and black is above
        if(color==Color.BLACK){
            //can't go above
            return (source.getRow()-destination.getRow()==-1) && source.getCol()==destination.getCol();
        }
        else{
            return (source.getRow()-destination.getRow()==1) && source.getCol()==destination.getCol();
        }
    }
}

class Cell{
    private Position position;
    private Piece piece;
    public Cell(Position position){
        this.position=position;
        this.piece=null;
    }
    public void setPiece(Piece piece){
        this.piece=piece;
    }
    public Piece getPiece(){
        return piece;
    }
    public Position getPosition(){
        return position;
    }
}
class Board{
    private Cell[][] board;
    public Board(){
        this.board=new Cell[8][8];
        initializeBoard();
    }
    private void initializeBoard(){
        createCells();
        //arrange black & white pawns
        setPawns();
        setRooks();
        setKnights();
        setBishops();
        setKings();
        setQueens();
    }
    private void createCells(){
        for(int row=0;row<8;row++){
            for(int col=0;col<8;col++){
                Position currentPosition=new Position(row, col);
                Cell currentCell=new Cell(currentPosition);
                setCell(currentCell, currentPosition);
            }
        }
    }
    private void setPawns(){
        for(int i=0;i<8;i++){
            Pawn blackPawn=new Pawn(PieceType.PAWN,Color.BLACK);
            Pawn whitePawn=new Pawn(PieceType.PAWN,Color.WHITE);
            board[1][i].setPiece(blackPawn);
            board[6][i].setPiece(whitePawn);
        }
    }
    private void setRooks(){
        board[0][0].setPiece(new Rook(PieceType.ROOK, Color.BLACK));
        board[0][7].setPiece(new Rook(PieceType.ROOK, Color.BLACK));
        board[7][0].setPiece(new Rook(PieceType.ROOK, Color.WHITE));
        board[7][7].setPiece(new Rook(PieceType.ROOK, Color.WHITE));
    }
    private void setKnights(){
        board[0][1].setPiece(new Knight(PieceType.KNIGHT, Color.BLACK));
        board[0][6].setPiece(new Knight(PieceType.KNIGHT, Color.BLACK));
        board[7][1].setPiece(new Knight(PieceType.KNIGHT, Color.WHITE));
        board[7][6].setPiece(new Knight(PieceType.KNIGHT, Color.WHITE));
    }
    private void setBishops(){
        board[0][2].setPiece(new Bishop(PieceType.BISHOP, Color.BLACK));
        board[0][5].setPiece(new Bishop(PieceType.BISHOP, Color.BLACK));
        board[7][2].setPiece(new Bishop(PieceType.BISHOP, Color.WHITE));
        board[7][5].setPiece(new Bishop(PieceType.BISHOP, Color.WHITE));
    }
    private void setKings(){
        board[0][4].setPiece(new King(PieceType.KING, Color.BLACK));
        board[7][4].setPiece(new King(PieceType.KING, Color.WHITE));
    }
    private void setQueens(){
        board[0][3].setPiece(new Queen(PieceType.QUEEN, Color.BLACK));
        board[7][3].setPiece(new Queen(PieceType.QUEEN, Color.WHITE));
    }
    public Cell getCell(Position position){
        return board[position.getRow()][position.getCol()];
    }
    public void setCell(Cell cell,Position position){
        board[position.getRow()][position.getCol()]=cell;
    }
    public Piece getPiece(Position position){return board[position.getRow()][position.getCol()].getPiece();}
}
class Player{
    private String name;
    private Color color;
    public Player(String name, Color color){
        this.name=name;
        this.color=color;
    }
    public String getName(){return name;}
    public Color getColor(){return color;}
}

class Move{
    private Position source;
    private Position destination;
    private Player player;
    public Move(Position source, Position destination, Player player){
        this.source=source;
        this.destination=destination;
        this.player=player;
    }
    public Position getSource(){return source;}
    public Position getDestination(){return destination;}
    public Player getPlayer(){return player;}
}

class ChessGame{
    private Board board;
    private Player whitePlayer;
    private Player blackPlayer;
    private Player currentPlayer;
    private GameStatus gameStatus;
    public ChessGame(Player whitePlayer, Player blackPlayer){
        this.board=new Board();
        this.whitePlayer=whitePlayer;
        this.blackPlayer=blackPlayer;
        this.currentPlayer=whitePlayer;
        this.gameStatus=GameStatus.IN_PROGRESS;
    }
    public void makeMove(Move move){
        Position sourcePosition=move.getSource();
        Position destinationPosition=move.getDestination();
        Cell sourceCell=board.getCell(sourcePosition);
        Cell destinationCell=board.getCell(destinationPosition);
        Piece piece=sourceCell.getPiece();
        Piece destinationPiece = destinationCell.getPiece();
        if(sourcePosition.getRow()==destinationPosition.getRow()
            && sourcePosition.getCol()==destinationPosition.getCol()){
            System.out.println("please make a move");
        }
        if(piece==null){
            System.out.println("Invalid Move! (no piece to move)");
        }
        else if(move.getPlayer()!=currentPlayer){
            System.out.println("Invalid Move! (wrong player trying to make move)");
        }
        else if(piece.getColor()!=currentPlayer.getColor()){
            System.out.println("Invalid Move! (trying to move opponents piece)");
        }
        else if(destinationPiece != null &&
            destinationPiece.getColor() == piece.getColor()){
                System.out.println("Invalid Move! (self capture)");
        }
        else if(!piece.canMove(board, sourcePosition, destinationPosition)){
            System.out.println("Invalid Move! (this piece can't move like that)");
        }
        else{
            Piece sourcePiece=sourceCell.getPiece();
            sourceCell.setPiece(null);
            destinationCell.setPiece(sourcePiece);
            currentPlayer=(currentPlayer.getColor()==Color.BLACK)? whitePlayer: blackPlayer;
            System.out.println("successfully moved the piece");
        }
    }
}

public class chess_basic {
    public static void main(String[] args) {
        Player dk=new Player("dk", Color.BLACK);
        Player md=new Player("md", Color.WHITE);
        ChessGame chessGame=new ChessGame(md,dk);
        Move move1=new Move(new Position(1, 1), new Position(2, 1), dk);
        chessGame.makeMove(move1);  
        Move move2=new Move(new Position(1, 1), new Position(2, 1), dk);
        chessGame.makeMove(move2);  
        // Move move3=new Move(new Position(6, 2), new Position(5, 2), dk);
        // chessGame.makeMove(move3);
        Move move3=new Move(new Position(6, 2), new Position(5, 2), md);
        chessGame.makeMove(move3);
        Move move4=new Move(new Position(6, 3), new Position(5, 3), dk);
        chessGame.makeMove(move4);
    }
}
