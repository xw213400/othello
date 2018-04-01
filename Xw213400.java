package xw213400;

import java.awt.Point;

import com.ibm.crl.contest.AbstractPlayer;
import com.ibm.crl.contest.ChessContext;

public class Xw213400 extends AbstractPlayer
{	
	ChessContext chessContext;
	static int deep;
	static int currStep;
	static Board board;
	static int [][] historyScore;
	static Move globle_tem_move;
	static Move best_move;
	static Move bak_move;
	static MoveArray[] move_matrix;
	static int[] move_mobility;
	static long start;
	static int shallowTime;
	static int last_result;
	static boolean timeOut;
	
	@Override
	public int init(ChessContext chessContext, byte[][] chessboard, int timeLimit, int memoryLimit, boolean rowor) 
	{
		this.chessContext = chessContext;
		
	    currStep = chessContext.getMyColor() ? -1 : 0;

	    board = new Board(chessboard, chessContext.getMyColor(), 
	    		(byte)chessContext.getChessboardWidth(), 
	    		(byte)chessContext.getChessboardHeight());
	   
	    historyScore = new int[Board.BOARD_SIZE][2];
	    move_matrix = new MoveArray[20];
	    for(int i = 0; i != 20; ++i)
	    {
	    	move_matrix[i] = new MoveArray(50);
	    }
	    
	    globle_tem_move = new Move();
	    best_move = new Move();
		bak_move = new Move();
		
		move_mobility = new int[20];
	    
		return 0;
	}
		
	@Override
	public int myTurn(byte[][] chessboard, Point[] availables, int curStep, Point lastMove) 
	{
		System.out.println("/******" + curStep + "******/");
		if(curStep - currStep == 2)
		{
			currStep = curStep;
		    if(curStep != 1 && lastMove != null)
		    {
		    	int lastStep = (lastMove.y+1)*(chessContext.getChessboardWidth()+1) + lastMove.x + 1;
		    	board.get_move(lastStep);
			    board.flip_Board(globle_tem_move);
		    }
		    if(curStep != 1 && lastMove == null) {
				Board.player = (byte)(1 - Board.player);
				Board.hash_code ^= HashTable.SWAP;
			}
		}
		else
		{
			currStep = curStep;
			board.set_Board(chessboard, 
					(byte)chessContext.getChessboardWidth(), 
					(byte)chessContext.getChessboardHeight());
		}

		if(availables.length != 0)
		{	
			return choose(availables);
		}
		else
	    {
			Board.player = (byte)(1 - Board.player);
			Board.hash_code ^= HashTable.SWAP;
	    	return -1;
	    }
	}
	
	private int choose(Point[] availables)
	{
		start = System.currentTimeMillis();
		if(Board.n_empties > 16)
		{
			for(int i = 0; i != Board.BOARD_SIZE; i++)
			{
				historyScore[i][0] = 0;
				historyScore[i][1] = 0;
			}
			shallowTime = (int)((chessContext.getTotalLeftTime() - 4200) / (Board.n_empties - 16) * 0.18);
			if(shallowTime <= 0 || shallowTime > 600)
				shallowTime = 100;
			for(deep = 6; ; )
			{
				last_result = MTD(last_result, deep);
				HashTable.transTable.clear();
				if(deep == 12 || System.currentTimeMillis()-start >= shallowTime)
					break;
				deep += 2;
			}
			System.out.println(last_result + ", " + deep + ", " + (System.currentTimeMillis()-start));
		}
		else
		{
			board.set_empties();
			if(Board.n_empties == 16 || Board.n_empties == 15)
				last_result /= 30;
			deep = 16;
			shallowTime = chessContext.getTotalLeftTime() != 0 ? chessContext.getTotalLeftTime() / 2 : 1000000;
			start = System.currentTimeMillis();
			timeOut = false;
			int this_result = MTD_exact(last_result, deep);
			HashTable.transTable.clear();
			if(timeOut && (this_result <= last_result || this_result < 0))
			{
				deep = 8;
				last_result = MTD(this_result * 30, deep);
				HashTable.transTable.clear();
			}
			last_result = this_result;
			System.out.println(last_result + ", " + timeOut + ", " + (System.currentTimeMillis()-start));
		}
		////////////////////////////////////////////////////////////
		int n = 0;		
		board.flip_Board(best_move);
		for(int i = 0; i != availables.length; i++)
		{
			if(best_move.point == (availables[i].y + 1) * (chessContext.getChessboardWidth() + 1) + availables[i].x + 1)
			{
				n = i;
				break;
			}
		}
		return n;
	}
	
	private int MTD_exact(int firstguess, int depth)
	{
		int g, lower, upper, beta;
		g = firstguess;
		upper = 10000;
		lower = -10000;
		while(lower < upper)
		{
			if(g >= firstguess)
				best_move.copy(bak_move);
			beta = (g == lower ? g + 1 : g);
			g = alpha_beta_exact(depth, beta-1, beta, false, 0);
			if(g < beta)
				upper = g;
			else
				lower = g;
			if(g < firstguess)
				best_move.copy(bak_move);
			if(timeOut)
				return g;
		}
		return g;
	}
	
	private int alpha_beta_exact(int deepCur, int alpha, int beta, boolean isPass, int savePlace)
	{
		if(System.currentTimeMillis() - start > shallowTime)
		{
			timeOut = true;
			if(Board.player == Board.MY_SIDE)
				return -10000;
			else
				return 10000;
		}
		
		int score, bestscore = -10000;
		
		if(HashTable.transTable.containsKey(Board.hash_code))
		{
			Hash pos = HashTable.transTable.get(Board.hash_code);
			if(pos.upper <= alpha)
				return pos.upper;
			else if(pos.lower >= beta)
				return pos.lower;
		}
		
		if(Board.n_empties <= 14)
			get_moves_fastfirst(savePlace);
		else
			get_moves_end(savePlace);
		
		if(move_matrix[savePlace].size == 0)
		{
			if(isPass)
			{
				if(Board.player == Board.MY_SIDE)
					return Board.n_diffdiscs;
				else
					return -Board.n_diffdiscs;
			}
			else
			{
				Board.player = (byte)(1 - Board.player);
				Board.hash_code ^= HashTable.SWAP;
				bestscore = -alpha_beta_exact(deepCur-1, -beta, -alpha, true, savePlace);
				Board.player = (byte)(1 - Board.player);
				Board.hash_code ^= HashTable.SWAP;
				return bestscore;
			}
		}
		
		Move move = null;
		for(int i = 0; i != move_matrix[savePlace].size; ++i)
		{
			move = move_matrix[savePlace].moves[i];
			board.flip_Board_no_edge(move);
			if(Board.n_empties == 7)
				score = -alpha_beta_parity_exact(deepCur-1, -beta, -alpha, false, savePlace+1);
			else
				score = -alpha_beta_exact(deepCur-1, -beta, -alpha, false, savePlace+1);
			board.untread_flip_Board_no_edge(move);
			if(score > bestscore)
			{
				bestscore = score;
				if(deepCur == deep)
					bak_move.copy(move);
				if(score >= beta)
				{
					if(savePlace <= 11)
						Board.hashTable.add(alpha, beta, bestscore);
					historyScore[move.point][Board.player] += 0x1<<deepCur;
					return bestscore;
				}
				if(score > alpha)
				{
					alpha = score;
				}
			}
		}
		if(savePlace <= 11)
			Board.hashTable.add(alpha, beta, bestscore);
		historyScore[move.point][Board.player] += 0x1<<deepCur;
		return bestscore;
	}
		
	private int alpha_beta_parity_exact(int deepCur, int alpha, int beta, boolean isPass, int savePlace)
	{
		int score, bestscore = -10000;
		
		if(HashTable.transTable.containsKey(Board.hash_code))
		{
			Hash pos = HashTable.transTable.get(Board.hash_code);
			if(pos.upper <= alpha)
				return pos.upper;
			else if(pos.lower >= beta)
				return pos.lower;
		}
		
		if(Board.n_empties == 7)
			Board.hole.init_Hole();
		
		get_moves_parity(savePlace);
		
		if(move_matrix[savePlace].size == 0)
		{
			if(isPass)
			{
				if(Board.player == Board.MY_SIDE)
					return Board.n_diffdiscs;
				else
					return -Board.n_diffdiscs;
			}
			else
			{
				Board.player = (byte)(1 - Board.player);
				Board.hash_code ^= HashTable.SWAP;
				bestscore = -alpha_beta_parity_exact(deepCur-1, -beta, -alpha, true, savePlace);
				Board.player = (byte)(1 - Board.player);
				Board.hash_code ^= HashTable.SWAP;
				return bestscore;
			}
		}
		
		Move move = null;
		for(int i = 0; i != move_matrix[savePlace].size; i++)
		{
			move = move_matrix[savePlace].moves[i];
			board.flip_Board_include_parity(move);
			if(Board.n_empties == 3)
				score = -alpha_beta_nopar_exact(deepCur-1, -beta, -alpha, false, savePlace+1);
			else
				score = -alpha_beta_parity_exact(deepCur-1, -beta, -alpha, false, savePlace+1);
			board.untread_flip_Board_include_parity(move);
			if(score > bestscore)
			{
				bestscore = score;
				if(score >= beta)
				{
					if(savePlace <= 11)
						Board.hashTable.add(alpha, beta, bestscore);
					return bestscore;
				}
				if(score > alpha)
				{
					alpha = score;
				}
			}
		}
		if(savePlace <= 11)
			Board.hashTable.add(alpha, beta, bestscore);
		return bestscore;
	}

	private int alpha_beta_nopar_exact(int deepCur, int alpha, int beta, boolean isPass, int savePlace)
	{
		int score = 0, bestscore = -10000;
		
		if(Board.n_empties == 1)
		{
			int point;
			if(Board.square[Hole.hole.intNum[0]] == Board.EMPTY)
				point = Hole.hole.intNum[0];
			else if(Board.square[Hole.hole.intNum[1]] == Board.EMPTY)
				point = Hole.hole.intNum[1];
			else if(Board.square[Hole.hole.intNum[2]] == Board.EMPTY)
				point = Hole.hole.intNum[2];
			else if(Board.square[Hole.hole.intNum[3]] == Board.EMPTY)
				point = Hole.hole.intNum[3];
			else if(Board.square[Hole.hole.intNum[4]] == Board.EMPTY)
				point = Hole.hole.intNum[4];
			else if(Board.square[Hole.hole.intNum[5]] == Board.EMPTY)
				point = Hole.hole.intNum[5];
			else
				point = Hole.hole.intNum[6];
			
			if(board.get_move(point))
			{
				if(Board.player == Board.MY_SIDE)
					bestscore = Board.n_diffdiscs + (globle_tem_move.eat.size << 1) + 1;
				else
					bestscore = Board.n_diffdiscs - (globle_tem_move.eat.size << 1) - 1;
			}
			else
			{
				Board.player = (byte)(1 - Board.player);
				if(board.get_move(point))
				{
					if(Board.player == Board.MY_SIDE)
						bestscore = Board.n_diffdiscs + (globle_tem_move.eat.size << 1) + 1;
					else
						bestscore = Board.n_diffdiscs - (globle_tem_move.eat.size << 1) - 1;
				}
				else
				{
					bestscore = Board.n_diffdiscs;
				}
				Board.player = (byte)(1 - Board.player);
			}
			
			if(Board.player == Board.OP_SIDE)
				bestscore = -bestscore;
			return bestscore;
		}
		
		get_moves_no_parity(savePlace);
		
		if(move_matrix[savePlace].size == 0)
		{
			if(isPass)
			{
				if(Board.player == Board.MY_SIDE)
					return Board.n_diffdiscs;
				else
					return -Board.n_diffdiscs;
			}
			else
			{
				Board.player = (byte)(1 - Board.player);
				bestscore = -alpha_beta_nopar_exact(deepCur-1, -beta, -alpha, true, savePlace);
				Board.player = (byte)(1 - Board.player);
				return bestscore;
			}
		}
		
		Move move = null;
		for(int i = 0; i != move_matrix[savePlace].size; ++i)
		{
			move = move_matrix[savePlace].moves[i];
			board.flip_Board_no_parity(move);
			score = -alpha_beta_nopar_exact(deepCur-1, -beta, -alpha, false, savePlace+1);
			board.untread_flip_Board_no_parity(move);
			if(score > bestscore)
			{
				bestscore = score;
				if(score >= beta)
				{
					return bestscore;
				}
				if(score > alpha)
				{
					alpha = score;
				}
			}
		}
		return bestscore;
	}

	private int MTD(int firstguess, int depth)
	{
		int g, lower, upper, beta;
		g = firstguess;
		upper = 1000000;
		lower = -1000000;
		while(lower < upper)
		{
			if(g >= firstguess)
				best_move.copy(bak_move);
			beta = (g == lower ? g + 1 : g);
			g = alpha_beta(depth, beta-1, beta, false, 0);
			if(g < beta)
				upper = g;
			else
				lower = g;
			if(g < firstguess)
				best_move.copy(bak_move);
		}
		return g;
	}
	
	private int alpha_beta(int deepCur, int alpha, int beta, boolean isPass, int savePlace)
	{
		int score, bestscore = -1000000;
		
		if(HashTable.transTable.containsKey(Board.hash_code))
		{
			Hash pos = HashTable.transTable.get(Board.hash_code);
			if(pos.upper <= alpha)
				return pos.upper;
			else if(pos.lower >= beta)
				return pos.lower;
		}
		
		if(deepCur == 0)
		{
			if(Board.player == Board.MY_SIDE)
				return board.evaluate();
			else
				return -board.evaluate();
		}
		
		get_moves(savePlace);
		
		if(move_matrix[savePlace].size == 0)
		{
			if(isPass)
			{
				if(Board.player == Board.MY_SIDE)
				{
					if(Board.n_diffdiscs > 0)
						return Board.n_diffdiscs + 90000;
					else if(Board.n_diffdiscs < 0)
						return Board.n_diffdiscs - 90000;
					else
						return Board.n_diffdiscs;
				}
				else
				{
					if(Board.n_diffdiscs > 0)
						return -Board.n_diffdiscs - 90000;
					else if(Board.n_diffdiscs < 0)
						return -Board.n_diffdiscs + 90000;
					else
						return -Board.n_diffdiscs;
				}
			}
			else
			{
				Board.player = (byte)(1 - Board.player);
				Board.hash_code ^= HashTable.SWAP;
				bestscore = -alpha_beta(deepCur-1, -beta, -alpha, true, savePlace);
				Board.player = (byte)(1 - Board.player);
				Board.hash_code ^= HashTable.SWAP;
				return bestscore;
			}
		}
		
		Move move = null;
		for(int i = 0; i != move_matrix[savePlace].size; ++i)
		{
			move = move_matrix[savePlace].moves[i];
			board.flip_Board(move);
			score = -alpha_beta(deepCur-1, -beta, -alpha, false, savePlace+1);
			board.untread_flip_Board(move);
			if(score > bestscore)
			{
				bestscore = score;
				if(deepCur == deep)
					bak_move.copy(move);
				if(score >= beta)
				{
					Board.hashTable.add(alpha, beta, bestscore);
					historyScore[move.point][Board.player] += 0x1<<deepCur;
					return bestscore;
				}
				if(score > alpha)
				{
					alpha = score;
				}
			}
		}
		Board.hashTable.add(alpha, beta, bestscore);
		historyScore[move.point][Board.player] += 0x1<<deepCur;
		return bestscore;
	}
	
	private void get_moves_no_parity(int savePlace)
	{
		move_matrix[savePlace].size = 0;
		if(Board.square[Hole.hole.intNum[0]] == Board.EMPTY && board.get_move(Hole.hole.intNum[0]))
			move_matrix[savePlace].add(globle_tem_move);
		if(Board.square[Hole.hole.intNum[1]] == Board.EMPTY && board.get_move(Hole.hole.intNum[1]))
			move_matrix[savePlace].add(globle_tem_move);
		if(Board.square[Hole.hole.intNum[2]] == Board.EMPTY && board.get_move(Hole.hole.intNum[2]))
			move_matrix[savePlace].add(globle_tem_move);
		if(Board.square[Hole.hole.intNum[3]] == Board.EMPTY && board.get_move(Hole.hole.intNum[3]))
			move_matrix[savePlace].add(globle_tem_move);
		if(Board.square[Hole.hole.intNum[4]] == Board.EMPTY && board.get_move(Hole.hole.intNum[4]))
			move_matrix[savePlace].add(globle_tem_move);
		if(Board.square[Hole.hole.intNum[5]] == Board.EMPTY && board.get_move(Hole.hole.intNum[5]))
			move_matrix[savePlace].add(globle_tem_move);
		if(Board.square[Hole.hole.intNum[6]] == Board.EMPTY && board.get_move(Hole.hole.intNum[6]))
			move_matrix[savePlace].add(globle_tem_move);
	}

	private void get_moves_parity(int savePlace)
	{
		move_matrix[savePlace].size = 0;
		if(Board.square[Hole.hole.intNum[0]] == Board.EMPTY && board.get_move(Hole.hole.intNum[0]))
		{
			if(Hole.holePar[Hole.holeId[Hole.hole.intNum[0]]])
			{
				move_matrix[savePlace].insert(globle_tem_move, (short)0);
			}
			else
			{
				move_matrix[savePlace].add(globle_tem_move);
			}
		}
		if(Board.square[Hole.hole.intNum[1]] == Board.EMPTY && board.get_move(Hole.hole.intNum[1]))
		{
			if(Hole.holePar[Hole.holeId[Hole.hole.intNum[1]]])
			{
				move_matrix[savePlace].insert(globle_tem_move, (short)0);
			}
			else
			{
				move_matrix[savePlace].add(globle_tem_move);
			}
		}
		if(Board.square[Hole.hole.intNum[2]] == Board.EMPTY && board.get_move(Hole.hole.intNum[2]))
		{
			if(Hole.holePar[Hole.holeId[Hole.hole.intNum[2]]])
			{
				move_matrix[savePlace].insert(globle_tem_move, (short)0);
			}
			else
			{
				move_matrix[savePlace].add(globle_tem_move);
			}
		}
		if(Board.square[Hole.hole.intNum[3]] == Board.EMPTY && board.get_move(Hole.hole.intNum[3]))
		{
			if(Hole.holePar[Hole.holeId[Hole.hole.intNum[3]]])
			{
				move_matrix[savePlace].insert(globle_tem_move, (short)0);
			}
			else
			{
				move_matrix[savePlace].add(globle_tem_move);
			}
		}
		if(Board.square[Hole.hole.intNum[4]] == Board.EMPTY && board.get_move(Hole.hole.intNum[4]))
		{
			if(Hole.holePar[Hole.holeId[Hole.hole.intNum[4]]])
			{
				move_matrix[savePlace].insert(globle_tem_move, (short)0);
			}
			else
			{
				move_matrix[savePlace].add(globle_tem_move);
			}
		}
		if(Board.square[Hole.hole.intNum[5]] == Board.EMPTY && board.get_move(Hole.hole.intNum[5]))
		{
			if(Hole.holePar[Hole.holeId[Hole.hole.intNum[5]]])
			{
				move_matrix[savePlace].insert(globle_tem_move, (short)0);
			}
			else
			{
				move_matrix[savePlace].add(globle_tem_move);
			}
		}
		if(Board.square[Hole.hole.intNum[6]] == Board.EMPTY && board.get_move(Hole.hole.intNum[6]))
		{
			if(Hole.holePar[Hole.holeId[Hole.hole.intNum[6]]])
			{
				move_matrix[savePlace].insert(globle_tem_move, (short)0);
			}
			else
			{
				move_matrix[savePlace].add(globle_tem_move);
			}
		}
	}
	
	private void get_moves_fastfirst(int savePlace)
	{
		int mobility;
		move_matrix[savePlace].size = 0;
		byte op = (byte)(1 - Board.player);
		for(short i = 0; i != Board.empties.size; i++)
		{
			if(Board.square[Board.empties.intNum[i]] == Board.EMPTY && board.get_move(Board.empties.intNum[i]))
			{
				board.flip_Board_no_parity(globle_tem_move);
				mobility = board.mobility_fastfirst(op);
				board.untread_flip_Board_no_parity(globle_tem_move);
				boolean notadded = true;
				for(short j = 0; j != move_matrix[savePlace].size; j++)
				{
					if(mobility < move_mobility[j])
					{
						for(int k = move_matrix[savePlace].size; k != j; k--)
						{
							move_mobility[k] = move_mobility[k - 1];
						}
						move_mobility[j] = mobility;
						move_matrix[savePlace].insert(globle_tem_move, j);
						notadded = false;
						break;
					}
				}
				if(notadded)
				{
					move_mobility[move_matrix[savePlace].size] = mobility;
					move_matrix[savePlace].add(globle_tem_move);
				}
			}
		}
	}
	
	private void get_moves_end(int savePlace)
	{
		move_matrix[savePlace].size = 0;
		for(int i = 0; i != Board.empties.size; i++)
		{
			if(Board.square[Board.empties.intNum[i]] != Board.EMPTY)
				continue;
			if(board.get_move(Board.empties.intNum[i]))
			{
				boolean notadded = true;
				for(short j = 0; j != move_matrix[savePlace].size; j++)
				{
					if(historyScore[Board.empties.intNum[i]][Board.player] > historyScore[move_matrix[savePlace].moves[j].point][Board.player])
					{
						move_matrix[savePlace].insert(globle_tem_move, j);
						notadded = false;
						break;
					}
				}
				if(notadded)
					move_matrix[savePlace].add(globle_tem_move);
			}
		}
	}
		
	private void get_moves(int savePlace)
	{
		move_matrix[savePlace].size = 0;
		for(int i = Board.PRESORT.bound_corner; i != Board.PRESORT.bound_csquare; i++)
		{
			if(Board.edge[Board.PRESORT.square[i]] && board.get_move(Board.PRESORT.square[i]))
			{
				boolean notadded = true;
				for(short j = 0; j != move_matrix[savePlace].size; j++)
				{
					if(historyScore[Board.PRESORT.square[i]][Board.player] > historyScore[move_matrix[savePlace].moves[j].point][Board.player])
					{
						move_matrix[savePlace].insert(globle_tem_move, j);
						notadded = false;
						break;
					}
				}
				if(notadded)
					move_matrix[savePlace].add(globle_tem_move);
			}
		}
		for(int i = 0; i != Board.PRESORT.bound_corner; i++)
		{
			if(Board.edge[Board.PRESORT.square[i]] && board.get_move(Board.PRESORT.square[i]))
				move_matrix[savePlace].insert(globle_tem_move, (short)0);
		}
		for(int i = Board.PRESORT.bound_csquare; i != Board.TOTAL_SQUARE; i++)
		{
			if(Board.edge[Board.PRESORT.square[i]] && board.get_move(Board.PRESORT.square[i]))
				move_matrix[savePlace].add(globle_tem_move);
		}
	}
}
