import java.util.*;
import java.time.*;

//================================================================
// Class that holds node state and functionality. This is a "helper"
// class in java terminology.
class Node {
    // Member variables
    List<positionTicTacToe> m_board;
    int m_bestChildPos = -1;

    // Constructor
    public Node(List<positionTicTacToe> board) {
        // Shallow copy! The mark is manually removed later for speed!
        m_board = board;
    }

    // Returns a copy of the board.  Kinda dangerous because it is a shallow copy, but
    // whatever, this isn't a banking system or anything.
    public List<positionTicTacToe> getBoard() {
        return m_board;
    }

    // Adds a mark at the board position
    public void addMark(int pos, boolean maximizer) {
        int mark = 1;
        if (maximizer) mark = 1; else mark = 2;
        m_board.get(pos).state = mark;
    }

    // Removes a mark at the board position
    public void removeMark(int pos) {
        m_board.get(pos).state = 0;
    }

    // Sets the child position (move) that is the "best" move at this node
    public void setBestChildPos(int pos) {
        m_bestChildPos = pos;
    }

    // Gets the child position that is the "best" move at this node
    public int getBestChildPos() {
        return m_bestChildPos;
    }

    // Returns true if the position at pos is occupied (1 or 2), else returns false
    public boolean isOccupied(int pos) {
        int state = m_board.get(pos).state;
		return ((state == 1) || (state == 2));
    }

    // Spawns a child node of the current node by adding a move at the current position
    public Node spawn(int pos, boolean maximizer) {
        Node child = new Node(m_board);
        child.addMark(pos, maximizer);
        return child;
    }

}
//================================================================

public class aiTicTacToe {

	public int player; //1 for player 1 and 2 for player 2
    List<List<positionTicTacToe>> m_winningLines;

	private int getStateOfPositionFromBoard(positionTicTacToe position, List<positionTicTacToe> board)
	{
		//a helper function to get state of a certain position in the Tic-Tac-Toe board by given position TicTacToe
		int index = position.x*16+position.y*4+position.z;
		return board.get(index).state;
	}




    // Board is the state of the game at the given moment. Our job is to determine what the best move to make
    // is for the given player, as a positionTicTacToe(x,y,z). This is called only once.
	public positionTicTacToe myAIAlgorithm(List<positionTicTacToe> board, int player)
	{
        LocalTime startTime = LocalTime.now();

		//TODO: this is where you are going to implement your AI algorithm to win the game. The default is an AI randomly choose any available move.
		positionTicTacToe myNextMove = new positionTicTacToe(0,0,0);

        //------------------------------------------
        // DEFAULT RANDOM METHOD FOR COMPARISON
        if (false) {
		    do
			    {
				    Random rand = new Random();
				    int x = rand.nextInt(4);
				    int y = rand.nextInt(4);
				    int z = rand.nextInt(4);
				    myNextMove = new positionTicTacToe(x,y,z);
			    }while(getStateOfPositionFromBoard(myNextMove,board)!=0);
		    return myNextMove;
        }

        //------------------------------------------






        //==========================================
        // MY MINIMAX/ALPHA-BETA METHOD

        // Construct root node.  This will be the current state of the board.
        Node root = new Node(board); 
        

        //if (player == 2) DEPTH_TO_USE = 2;
        // starting at 4 wins, but ends up too slow later in the game. Tune adaptive depth

        //++++++++ PROBABLY DON"T USE THIS+++++++++++++
        // ADAPTIVE DEPTH - this is the AI's secret weapon! It will search harder later
        // in the game when it has the time and when it matters most!
        // Adjust depth based on moves remaining - we can afford to search deeper when
        // there are less empty spaces
        int usedSpaces = 0;
        for (int s=0;s<board.size();++s) if (board.get(s).state > 0) usedSpaces += 1;
        /*
        if (false) { // switch for testing (proven to be better than not doing it!)
            if (usedSpaces > 18) DEPTH_TO_USE += 1; // go 5 deep
            if (usedSpaces > 26) DEPTH_TO_USE += 1; // go 6 deep
            if (usedSpaces > 30) DEPTH_TO_USE += 1; // go 7 deep
        }
        */
        //+++++++++++++++++++++++++++++++++++++++++++++

        int initialAlpha = -2147483647; // essentially negative infinity
        int initialBeta =  2147483647; // essentially positive infinity
        boolean isMaximizer = true; // true for player 1, false for player 2 (P1 is maximizer, P2 is minimizer)
        if (player == 2) isMaximizer = false;


        // Progressive deepening loop
        int DEPTH_TO_START = 2;
        int MAX_DEPTH = 15;
        if (false) {
            DEPTH_TO_START = 2;
            MAX_DEPTH = 4;
        }

        int d = 0;
        int hvOfMove = 0;
        int finalHV = 0;
        for (d=DEPTH_TO_START;d<=MAX_DEPTH;++d) {
            // Run top level of recursion. Will naturally return the heuristic value
            // of the node, but that needs to be mapped to a specific move.
            // Note, importantly, that isMaximizer at the top level determines who the
            // resulting move is going to benefit the most.  If it is true, then
            // WE are the maximizer, and so we want high HV.  If it is false, then
            // WE are the minimizer, and so we want low HV.
            hvOfMove = alphabeta(root, d, initialAlpha, initialBeta, isMaximizer, startTime);

            // If we had to cut out early (as indicated by the return value -29457459), don't use!
            if (hvOfMove == -29457459) break; 
        
            finalHV = hvOfMove;

            // Map heurisitic value of the returned move to the actual position
            int moveIndex = root.getBestChildPos();
            int x = moveIndex/(4*4);
            int y = (moveIndex % (4*4)) / 4;
            int z = moveIndex % 4;
            myNextMove = new positionTicTacToe(x,y,z);
        }



        //==========================================



        LocalTime endTime = LocalTime.now(); 
        
        if (player == 1)
            System.out.println("time taken "+ Duration.between(startTime, endTime).toMillis() + " ms"); 
        
    	//System.out.println("Player " + player + " made a move at ");
        //myNextMove.printPosition();
        //System.out.printf("%d",player);
        System.out.println("Turn " + usedSpaces + ", player " + player + " HV: " + finalHV + ", depth reached: " + (d-1));
		return myNextMove;


	}

    //================================================================
    // This is a depth-first recursive algorithm that loops through all potential moves at the
    // given depth, pruning (breaking out) when it can.  It returns the heuristic
    // value, i.e. the score of the node.
    // Based on pseudocode at https://en.wikipedia.org/wiki/Alpha%E2%80%93beta_pruning
    // as suggested in the assignment description. This recurses through the tree
    // to an input depth
    private int alphabeta(Node node, int depth, int alpha, int beta, boolean maximizingPlayer, LocalTime startTime)
    {
        LocalTime now = LocalTime.now();
        long timeLeft = 5000l - Duration.between(startTime, now).toMillis();
        if (timeLeft < 100l) return -29457459; // return flag value to indicate timeout
        if (depth == 0 || (terminalCheck(node.getBoard()) != 0) || (timeLeft < 100)) {
        //if (depth == 0 || (terminalCheck(node.getBoard()) != 0)) {
            return getHeuristicValue(node.getBoard(), depth);
        }
        if (maximizingPlayer) {
            int value = -2000000001;
            // Loop through all possible moves
            for (int pos=0;pos<64;++pos) {
                if (node.isOccupied(pos)) continue; // Skip this position if it is already taken
                //System.out.println("Move remaining at " + pos);


                // Execute the move at the designated space, creating a child node
                Node child = node.spawn(pos, true);

                // Go down to the next level until it returns with its heuristic value.
                // Save the move as the calling node's best if it is the best that can be made yet at this level.
                // Java is essentially pass-by-reference, so this action will be returned to the caller.
                int childHV = alphabeta(child, depth-1, alpha, beta, false, startTime);

                node.removeMark(pos); // Undo move since we have now returned - more efficient than deep copy

                if (childHV > value) { // if best child thus far
                    value = childHV; // save this child's HV
                    node.setBestChildPos(pos); // save this child position as the best until something beats it
                    //System.out.println("Best position is now " + pos);
                }

                // See if we can stop the loop here to avoid unnecessary calculations
                alpha = Math.max(alpha, value);
                if (alpha >= beta) break; //(* beta cut-off *)
            } // end loop over children

            return value; // return the HV of the node
        } // end if maximizing player
        else {
            int value = 2000000001;
            // Loop through all possible moves
            for (int pos=0;pos<64;++pos) {
                if (node.isOccupied(pos)) continue; // Skip this position if it is already taken
                //System.out.println("Move remaining at " + pos);

                // Execute the move at the designated space, creating a child node
                Node child = node.spawn(pos, false);

                // Go down to the next level until it returns with its heuristic value
                // Save the move as the calling node's best if it is the best that can be made yet at this level.
                // Java is essentially pass-by-reference, so this action will be returned to the caller.
                int childHV = alphabeta(child, depth-1, alpha, beta, true, startTime);

                node.removeMark(pos); // Undo move since we have now returned - more efficient than deep copy

                if (childHV < value) { // if best child thus far
                    value = childHV; // save this child's HV
                    node.setBestChildPos(pos); // save this child position as the best until something beats it
                }

                // See if we can stop the loop here to avoid unnecessary calculations
                beta = Math.min(beta, value);
                if (alpha >= beta) break; //(* alpha cut-off *)
            } // end loop over children

            return value;
        } // end if minimizing player

    }
    //================================================================


    // Calculates the heuristic value of the node (note that this node ALREADY has
    // its state set for the move that was made, so we don't need to add it. Just
    // calculate the HV of the node.)  Positive is good for maximizer, negative is
    // good for minimizer.
    public int getHeuristicValue(List<positionTicTacToe> board, int depth) {
        // Initialize to a tiny random number to prevent ties
        //Random rand = new Random();
	    //double hv = (rand.nextDouble()-0.5) * 0.000001;
        int hv = 0;

        // HV algorithm - adds 1000 for 3 in a row, adds 100 for 2 in a row, adds 10 for one in a row 
        // (all unblocked). Algorithm courtesy of https://github.com/DevanshuSave/3DTicTacToe
        if (true) {
            for (int i=0;i<m_winningLines.size();++i) {
                List<positionTicTacToe> line = m_winningLines.get(i);
                int marks1=0, marks2=0;
                for(int j=0;j<=3;j++)
                {
                    int thisSpot = line.get(j).x*16+line.get(j).y*4+line.get(j).z;
                    if(board.get(thisSpot).state==1) {
                        marks1+=1;
                    }
                    else if(board.get(thisSpot).state==2) {
                        marks2+=1;
                    }
                }

                if(marks1>0 && marks2>0) continue;
                if(marks1>0){
                    if(marks1==1){
                        hv+=10;
                    }
                    else if(marks1==2){
                        hv+=100;
                    }
                    else if(marks1==3){
                        hv+=10000;
                    }
                }
                else if(marks2>0){
                    if(marks2==1){
                        hv-=10;//9;
                    }
                    else if(marks2==2){
                        hv-=100;//99;
                    }
                    else if(marks2==3){
                        hv-=10000;//9999;
                    }  
                }
            } // end loop over winning lines
        } // testing switch


        if (false) { // switch for testing
            // Nominal HV: how many remaining ways to win there are for player 1 minus
            // how many ways to win there are for player 2. Remember we have defined 
            // player 1 to be the maximizer and player 2 to be the minimizer.
            // Loop over the winning lines, and if they are either blank or belong
            // to one of the players, tack on one method for that player.
            int p1WaysToWin = 0;
            int p2WaysToWin = 0;
            for (int i=0;i<m_winningLines.size();++i) {
                List<positionTicTacToe> line = m_winningLines.get(i);
                // Each winning line contains a list of four spaces
                // Loop over those four spaces
                int thisLineP1 = 0;
                int thisLineP2 = 0;
                for (int s=0;s<line.size();++s) {
                    int thisSpot = line.get(s).x*16 + line.get(s).y*4 + line.get(s).z;
                    if (board.get(thisSpot).state == 1) thisLineP1 += 1;
                    if (board.get(thisSpot).state == 2) thisLineP2 += 1;
                }
                if (thisLineP2 == 0) p1WaysToWin += 1; // tack on one way to win for player 1 if p2 has none of the spaces
                if (thisLineP1 == 0) p2WaysToWin += 1; // tack on one way to win for player 2 if p1 has none of the spaces
            }
            int waysDelta = p1WaysToWin - p2WaysToWin;
            hv += waysDelta*16;
        } // end testing switch


        // Dinky little boost for center spaces, hopefully will speedup
        if (false) { // switch for testing. This absolutely improves things!
            //hv += (1.5-board.get(41).state)*0.01; // space 41 is (2,2,1)
            //hv += (1.5-board.get(42).state)*0.01; // space 42 is (2,2,2)
            if (board.get(42).state == 1) hv += 1;
            if (board.get(42).state == 2) hv -= 1;
            //if (board.get(41).state == 1) hv += 1;
            //if (board.get(41).state == 2) hv -= 1;

        }

        if (false) { // switch for testing  THIS IS SLOW AND DOESN'T DO MUCH. SKIP IT
            // Give an ever so small score boost to the preferred center spaces to break ties.
            // These have 1 and 2 xyz indices (instead of 0 and 4). So add points for who has them.
            for (int i=0;i<board.size();++i) {
                if (board.get(i).x == 1 || board.get(i).x == 2) // if center x space
                    if (board.get(i).state == 1) hv += 0.001; if (board.get(i).state == 2) hv -= 0.001; // slight boost to player who has it

                if (board.get(i).y == 1 || board.get(i).y == 2) // if center y space
                    if (board.get(i).state == 1) hv += 0.001; if (board.get(i).state == 2) hv -= 0.001; // slight boost to player who has it

                if (board.get(i).z == 1 || board.get(i).z == 2) // if center z space
                    if (board.get(i).state == 1) hv += 0.001; if (board.get(i).state == 2) hv -= 0.001; // slight boost to player who has it
            }

            // Tack on a little bit for the eight corners since they are worth a bit extra
            for (int i=0;i<board.size();++i) {
                if (board.get(i).x == 0 && board.get(i).y == 0 && board.get(i).z == 0) // if corner 1
                    if (board.get(i).state == 1) hv += 0.001; if (board.get(i).state == 2) hv -= 0.001; // slight boost to player who has it
                if (board.get(i).x == 3 && board.get(i).y == 3 && board.get(i).z == 3) // if corner 2
                    if (board.get(i).state == 1) hv += 0.001; if (board.get(i).state == 2) hv -= 0.001; // slight boost to player who has it
                if (board.get(i).x == 0 && board.get(i).y == 0 && board.get(i).z == 3) // if corner 3
                    if (board.get(i).state == 1) hv += 0.001; if (board.get(i).state == 2) hv -= 0.001; // slight boost to player who has it
                if (board.get(i).x == 0 && board.get(i).y == 3 && board.get(i).z == 0) // if corner 4
                    if (board.get(i).state == 1) hv += 0.001; if (board.get(i).state == 2) hv -= 0.001; // slight boost to player who has it
                if (board.get(i).x == 3 && board.get(i).y == 0 && board.get(i).z == 0) // if corner 5
                    if (board.get(i).state == 1) hv += 0.001; if (board.get(i).state == 2) hv -= 0.001; // slight boost to player who has it
                if (board.get(i).x == 3 && board.get(i).y == 3 && board.get(i).z == 0) // if corner 6
                    if (board.get(i).state == 1) hv += 0.001; if (board.get(i).state == 2) hv -= 0.001; // slight boost to player who has it
                if (board.get(i).x == 0 && board.get(i).y == 3 && board.get(i).z == 3) // if corner 7
                    if (board.get(i).state == 1) hv += 0.001; if (board.get(i).state == 2) hv -= 0.001; // slight boost to player who has it
                if (board.get(i).x == 3 && board.get(i).y == 0 && board.get(i).z == 3) // if corner 8
                    if (board.get(i).state == 1) hv += 0.001; if (board.get(i).state == 2) hv -= 0.001; // slight boost to player who has it

            }
        } // end testing switch

        // Make the HV positive or negative infinity if it will result in one player winning
        int winner = terminalCheck(board);
        if (winner == 1) hv = 2000000000 + depth; // + depth gives a slight boost to wins found at an earlier turn or losses at a later turn
        if (winner == 2) hv = -2000000000 - depth; // - depth gives a slight boost to wins found at an earlier turn or losses at a later turn
        if (winner == -1) hv = 0;

        return hv;
    }


    //================================================================
    // BASICALLY COPIES OF FUNCTIONS FROM runTicTacToe.java. No sense in re-writing them.


    // Check for terminal node (i.e. check if somebody has won the game with the current board state)
    private int terminalCheck(List<positionTicTacToe> board)
    {

		//brute-force
		for(int i=0;i<m_winningLines.size();i++)
		{

			positionTicTacToe p0 = m_winningLines.get(i).get(0);
			positionTicTacToe p1 = m_winningLines.get(i).get(1);
			positionTicTacToe p2 = m_winningLines.get(i).get(2);
			positionTicTacToe p3 = m_winningLines.get(i).get(3);

			int state0 = getStateOfPositionFromBoard(p0,board);
			int state1 = getStateOfPositionFromBoard(p1,board);
			int state2 = getStateOfPositionFromBoard(p2,board);
			int state3 = getStateOfPositionFromBoard(p3,board);

			//if they have the same state (marked by same player) and they are not all marked.
			if(state0 == state1 && state1 == state2 && state2 == state3 && state0!=0)
			{
				//someone wins
				p0.state = state0;
				p1.state = state1;
				p2.state = state2;
				p3.state = state3;

				//print the satisified winning line (one of them if there are several)
                // Don't do it in this version because we are just checking
				//p0.printPosition();
				//p1.printPosition();
				//p2.printPosition();
				//p3.printPosition();
				return state0;
			}
		}
		for(int i=0;i<board.size();i++) // check for all positions filled but no winner (cat's game)
		{
			if(board.get(i).state==0)
			{
				//game is not ended, continue
				return 0;
			}
		}
		return -1; //call it a draw
    }

    //================================================================




	private List<List<positionTicTacToe>> initializeWinningLines()
	{
		//create a list of winning line so that the game will "brute-force" check if a player satisfied any 	winning condition(s).
		List<List<positionTicTacToe>> winningLines = new ArrayList<List<positionTicTacToe>>();

		//48 straight winning lines
		//z axis winning lines
		for(int i = 0; i<4; i++)
			for(int j = 0; j<4;j++)
			{
				List<positionTicTacToe> oneWinCondtion = new ArrayList<positionTicTacToe>();
				oneWinCondtion.add(new positionTicTacToe(i,j,0,-1));
				oneWinCondtion.add(new positionTicTacToe(i,j,1,-1));
				oneWinCondtion.add(new positionTicTacToe(i,j,2,-1));
				oneWinCondtion.add(new positionTicTacToe(i,j,3,-1));
				winningLines.add(oneWinCondtion);
			}
		//y axis winning lines
		for(int i = 0; i<4; i++)
			for(int j = 0; j<4;j++)
			{
				List<positionTicTacToe> oneWinCondtion = new ArrayList<positionTicTacToe>();
				oneWinCondtion.add(new positionTicTacToe(i,0,j,-1));
				oneWinCondtion.add(new positionTicTacToe(i,1,j,-1));
				oneWinCondtion.add(new positionTicTacToe(i,2,j,-1));
				oneWinCondtion.add(new positionTicTacToe(i,3,j,-1));
				winningLines.add(oneWinCondtion);
			}
		//x axis winning lines
		for(int i = 0; i<4; i++)
			for(int j = 0; j<4;j++)
			{
				List<positionTicTacToe> oneWinCondtion = new ArrayList<positionTicTacToe>();
				oneWinCondtion.add(new positionTicTacToe(0,i,j,-1));
				oneWinCondtion.add(new positionTicTacToe(1,i,j,-1));
				oneWinCondtion.add(new positionTicTacToe(2,i,j,-1));
				oneWinCondtion.add(new positionTicTacToe(3,i,j,-1));
				winningLines.add(oneWinCondtion);
			}

		//12 main diagonal winning lines
		//xz plane-4
		for(int i = 0; i<4; i++)
			{
				List<positionTicTacToe> oneWinCondtion = new ArrayList<positionTicTacToe>();
				oneWinCondtion.add(new positionTicTacToe(0,i,0,-1));
				oneWinCondtion.add(new positionTicTacToe(1,i,1,-1));
				oneWinCondtion.add(new positionTicTacToe(2,i,2,-1));
				oneWinCondtion.add(new positionTicTacToe(3,i,3,-1));
				winningLines.add(oneWinCondtion);
			}
		//yz plane-4
		for(int i = 0; i<4; i++)
			{
				List<positionTicTacToe> oneWinCondtion = new ArrayList<positionTicTacToe>();
				oneWinCondtion.add(new positionTicTacToe(i,0,0,-1));
				oneWinCondtion.add(new positionTicTacToe(i,1,1,-1));
				oneWinCondtion.add(new positionTicTacToe(i,2,2,-1));
				oneWinCondtion.add(new positionTicTacToe(i,3,3,-1));
				winningLines.add(oneWinCondtion);
			}
		//xy plane-4
		for(int i = 0; i<4; i++)
			{
				List<positionTicTacToe> oneWinCondtion = new ArrayList<positionTicTacToe>();
				oneWinCondtion.add(new positionTicTacToe(0,0,i,-1));
				oneWinCondtion.add(new positionTicTacToe(1,1,i,-1));
				oneWinCondtion.add(new positionTicTacToe(2,2,i,-1));
				oneWinCondtion.add(new positionTicTacToe(3,3,i,-1));
				winningLines.add(oneWinCondtion);
			}

		//12 anti diagonal winning lines
		//xz plane-4
		for(int i = 0; i<4; i++)
			{
				List<positionTicTacToe> oneWinCondtion = new ArrayList<positionTicTacToe>();
				oneWinCondtion.add(new positionTicTacToe(0,i,3,-1));
				oneWinCondtion.add(new positionTicTacToe(1,i,2,-1));
				oneWinCondtion.add(new positionTicTacToe(2,i,1,-1));
				oneWinCondtion.add(new positionTicTacToe(3,i,0,-1));
				winningLines.add(oneWinCondtion);
			}
		//yz plane-4
		for(int i = 0; i<4; i++)
			{
				List<positionTicTacToe> oneWinCondtion = new ArrayList<positionTicTacToe>();
				oneWinCondtion.add(new positionTicTacToe(i,0,3,-1));
				oneWinCondtion.add(new positionTicTacToe(i,1,2,-1));
				oneWinCondtion.add(new positionTicTacToe(i,2,1,-1));
				oneWinCondtion.add(new positionTicTacToe(i,3,0,-1));
				winningLines.add(oneWinCondtion);
			}
		//xy plane-4
		for(int i = 0; i<4; i++)
			{
				List<positionTicTacToe> oneWinCondtion = new ArrayList<positionTicTacToe>();
				oneWinCondtion.add(new positionTicTacToe(0,3,i,-1));
				oneWinCondtion.add(new positionTicTacToe(1,2,i,-1));
				oneWinCondtion.add(new positionTicTacToe(2,1,i,-1));
				oneWinCondtion.add(new positionTicTacToe(3,0,i,-1));
				winningLines.add(oneWinCondtion);
			}

		//4 additional diagonal winning lines
		List<positionTicTacToe> oneWinCondtion = new ArrayList<positionTicTacToe>();
		oneWinCondtion.add(new positionTicTacToe(0,0,0,-1));
		oneWinCondtion.add(new positionTicTacToe(1,1,1,-1));
		oneWinCondtion.add(new positionTicTacToe(2,2,2,-1));
		oneWinCondtion.add(new positionTicTacToe(3,3,3,-1));
		winningLines.add(oneWinCondtion);

		oneWinCondtion = new ArrayList<positionTicTacToe>();
		oneWinCondtion.add(new positionTicTacToe(0,0,3,-1));
		oneWinCondtion.add(new positionTicTacToe(1,1,2,-1));
		oneWinCondtion.add(new positionTicTacToe(2,2,1,-1));
		oneWinCondtion.add(new positionTicTacToe(3,3,0,-1));
		winningLines.add(oneWinCondtion);

		oneWinCondtion = new ArrayList<positionTicTacToe>();
		oneWinCondtion.add(new positionTicTacToe(3,0,0,-1));
		oneWinCondtion.add(new positionTicTacToe(2,1,1,-1));
		oneWinCondtion.add(new positionTicTacToe(1,2,2,-1));
		oneWinCondtion.add(new positionTicTacToe(0,3,3,-1));
		winningLines.add(oneWinCondtion);

		oneWinCondtion = new ArrayList<positionTicTacToe>();
		oneWinCondtion.add(new positionTicTacToe(0,3,0,-1));
		oneWinCondtion.add(new positionTicTacToe(1,2,1,-1));
		oneWinCondtion.add(new positionTicTacToe(2,1,2,-1));
		oneWinCondtion.add(new positionTicTacToe(3,0,3,-1));
		winningLines.add(oneWinCondtion);

		return winningLines;

	}

    // Constructor
	public aiTicTacToe(int setPlayer)
	{
		player = setPlayer;
        m_winningLines = initializeWinningLines();
	}
}
