package xw213400;

import java.util.Vector;

class Board
{
	static byte square[];
	static boolean[] edge;
	static byte player;
	static short n_diffdiscs; 
	static short n_empties;
	static long hash_code;
	static HashTable hashTable;
	static byte[] tem_array;
	
	static Hole hole;
	static IntArray empties;
	
	static short BOARD_SIZE;
	static short BEGINPOINT;
	static short ENDPOINT;
	static short TOTAL_SQUARE;
	static byte MY_SIDE;
	static byte OP_SIDE;
	static final byte EMPTY = 2;
	static final byte WALL = 3;
	static byte LEFTUP, UP, RIGHTUP, LEFT, RIGHT, LEFTDOWN, DOWN, RIGHTDOWN;
	static byte[] DIRECTION;
	static short MAX_FLIPS;
	static Vector<Corner> CORNER;
	static PreSort PRESORT;
	static int STEP1;
	static int STEP2;
	
	Board(byte[][] board, boolean myColor, byte colSize, byte rowSize)
	{
		MY_SIDE = myColor ? (byte)0 : (byte)1;
		OP_SIDE = (byte)(1 - MY_SIDE);
		BOARD_SIZE = (short)((colSize+1)*(rowSize+2)+1);
		BEGINPOINT = (short)((colSize+1)*(rowSize+1)-1);
		ENDPOINT = (short)(colSize+1);
		
		LEFTUP = (byte)(-colSize-2);
	    UP = (byte)(-colSize-1);
	    RIGHTUP = (byte)(-colSize);
	    LEFT = (byte)(-1);
	    RIGHT = (byte)(1);
	    LEFTDOWN = (byte)(colSize);
	    DOWN = (byte)(colSize+1);
	    RIGHTDOWN = (byte)(colSize+2);
	    
	    DIRECTION = new byte[8];
	    DIRECTION[0] = UP;
	    DIRECTION[1] = DOWN;
	    DIRECTION[2] = LEFT;
	    DIRECTION[3] = RIGHT;
	    DIRECTION[4] = LEFTUP;
	    DIRECTION[5] = RIGHTDOWN;
	    DIRECTION[6] = LEFTDOWN;
	    DIRECTION[7] = RIGHTUP;
	    
	    byte minSize = (byte)Math.min(colSize, rowSize);
	    byte maxSize = (byte)Math.max(colSize, rowSize);	    
	    MAX_FLIPS = (short)((maxSize-3) + (minSize-3)*3);
		
		square = new byte[BOARD_SIZE];
		for(int i = 0; i != BOARD_SIZE; ++i)
	    {
	    	int col = i%(colSize+1)-1;
	    	int row = i/(colSize+1)-1;
	    	if(col >= 0 && col < colSize && row >= 0 && row < rowSize)
	    	{
	    		if(board[col][row] == MY_SIDE + 1)
	    			square[i] = MY_SIDE;
	    		else if(board[col][row] == OP_SIDE + 1)
	    			square[i] = OP_SIDE;
	    		else if(board[col][row] == Xw213400.AVAILABLE)
	    		{
	    			square[i] = EMPTY;
	    			n_empties++;
	    		}
	    		else
	    			square[i] = WALL;
	    	}
	    	else
	    		square[i] = WALL;
	    }
		
		for(int i = BEGINPOINT; i != ENDPOINT; i--)
		{
			if(square[i] == EMPTY && !can_move(i))
			{
				square[i] = WALL;
				n_empties--;
			}
		}
		
		TOTAL_SQUARE = n_empties;
		player = 0;
		n_diffdiscs = 0;
	    
	    edge = new boolean[BOARD_SIZE];
	    set_edge();
	    
	    init_CORNER_PRESORT(board, colSize, rowSize);
	    
	    hashTable = new HashTable(BOARD_SIZE);
	    init_hash_code();
	    
	    empties = new IntArray(18);
	    hole = new Hole();
	    
	    tem_array = new byte[BOARD_SIZE];

	    STEP1 = (int)(TOTAL_SQUARE * 0.4);
		STEP2 = (int)(TOTAL_SQUARE * 0.6);
	}
	
	private boolean can_move(int point)
	{
		for(int i = 7; i != -1; i--)
		{
			if(square[point + DIRECTION[i]] == EMPTY && square[point + (DIRECTION[i] << 1)] == EMPTY)
				return true;
		}
		return false;
	}
	
	private boolean countSquare(int point, byte kind, byte upper)
	{
		int n = 0;
		if(square[point + DIRECTION[0]] == kind)
		{
			n++;
			if(n > upper)
				return true;
		}
		if(square[point + DIRECTION[1]] == kind)
		{
			n++;
			if(n > upper)
				return true;
		}
		if(square[point + DIRECTION[2]] == kind)
		{
			n++;
			if(n > upper)
				return true;
		}
		if(square[point + DIRECTION[3]] == kind)
		{
			n++;
			if(n > upper)
				return true;
		}
		if(square[point + DIRECTION[4]] == kind)
		{
			n++;
			if(n > upper)
				return true;
		}
		if(square[point + DIRECTION[5]] == kind)
		{
			n++;
			if(n > upper)
				return true;
		}
		if(square[point + DIRECTION[6]] == kind)
		{
			n++;
			if(n > upper)
				return true;
		}
		if(square[point + DIRECTION[7]] == kind)
		{
			n++;
			if(n > upper)
				return true;
		}
		return false;
	}
	
	private void set_edge()
	{
	    for(int i = BEGINPOINT; i != ENDPOINT; i--)
	    {
	    	if(square[i] == EMPTY && (countSquare(i, MY_SIDE, (byte)0) || countSquare(i, OP_SIDE, (byte)0)))
	    		edge[i] = true;
	    	else
	    		edge[i] = false;
	    }
	}
	
	private void init_CORNER_PRESORT(byte[][] board, byte colSize, byte rowSize) 
	{
	    CORNER = new Vector<Corner>();
	    PRESORT = new PreSort(TOTAL_SQUARE);

	    int[] boardInfo = new int[BOARD_SIZE];
	    int[] cSquare = new int[BOARD_SIZE];
	    int[] xSquare = new int[BOARD_SIZE];
	    int[] coSquare = new int[BOARD_SIZE];
	    int[] sSquare = new int[BOARD_SIZE];

	    for(int i = 0; i != BOARD_SIZE; i++)
	    {
	    	if(square[i] == EMPTY && is_corner(i))
	    	{
	    		Corner cnr = new Corner();
				cnr.position = i;
				for(int j = 0; j != 4; j++)
				{
					for(int point = i + DIRECTION[j]; square[point] == EMPTY && is_edge(point, j); point += DIRECTION[j])
					{
						cnr.effection[j]++;
						boardInfo[point] = 1;
					}
					if(cnr.effection[j] >= 2)
					{
						cnr.neighbor[j] = true;
						cSquare[i + DIRECTION[j]] -= 8;
						cSquare[i + (DIRECTION[j] << 1)] += 3;
						cnr.cSquare.add(i + DIRECTION[j]);
						cnr.eff_corner += cnr.effection[j];
					}
				}
				if(cnr.eff_corner != 0)
				{
					cnr.eff_corner += 2;
					for(int j = 4; j != 8; j++)
    				{
    					if(square[i + DIRECTION[j]] == EMPTY)
    					{
    						if(square[i + (DIRECTION[j] << 1)] == EMPTY)
	    					{
    							xSquare[i + DIRECTION[j]] -= cnr.eff_corner * 10;
    							xSquare[i + (DIRECTION[j] << 1)] += 1;
    							cnr.xSquare.add(i + DIRECTION[j]);
	    					}
    					}
    				}
					for(int j = 0; j != 4; j++)
    				{
    					if(square[i + DIRECTION[j]] == EMPTY && !is_edge(i + DIRECTION[j], j))
    					{
    						if(square[i + (DIRECTION[j] << 1)] == EMPTY)
	    					{
    							xSquare[i + DIRECTION[j]] -= cnr.eff_corner * 10;
    							xSquare[i + (DIRECTION[j] << 1)] += 1;
    							cnr.xSquare.add(i + DIRECTION[j]);
	    					}
    					}
    				}
					coSquare[i] += cnr.eff_corner * 20;
				}
				CORNER.add(cnr);
				sSquare[i] += 20;
	    	}
	    }

	    for(int i = 0; i != BOARD_SIZE; i++)
	    {
	    	if(square[i] == EMPTY)
	    		boardInfo[i] += cSquare[i] + xSquare[i] + coSquare[i] + sSquare[i];
	    	else
	    		boardInfo[i] = -10000;
	    }
	    
	    for(int size = 0; size != TOTAL_SQUARE;)
	    {
	    	int index_max = 0;
	    	for(int i = 0; i != Board.BOARD_SIZE; i++)
		    {
		    	if(boardInfo[i] != -10000)
		    		index_max = boardInfo[i]>boardInfo[index_max] ? i : index_max;
		    }
		    boardInfo[index_max] = -10000;
		    PRESORT.square[size] = index_max;
		    size++;
	    }
	    
	    PRESORT.bound_corner = CORNER.size();
	    for(int i = 0; i != TOTAL_SQUARE; i++)
	    {
	    	if(cSquare[PRESORT.square[i]] < 0)
	    		PRESORT.bound_csquare--;
	    	if(xSquare[PRESORT.square[i]] < 0)
	    	{
	    		PRESORT.bound_csquare--;
	    		PRESORT.bound_xsquare--;
	    	}
	    }
	}
	
	private boolean is_corner(int i)
	{
		if(!((square[i+LEFTUP]==EMPTY && square[i+RIGHTDOWN]==EMPTY) || 
				(square[i+LEFT]==EMPTY && square[i+RIGHT]==EMPTY) || 
				(square[i+UP]==EMPTY && square[i+DOWN]==EMPTY)||
				(square[i+LEFTDOWN]==EMPTY && square[i+RIGHTUP]==EMPTY)) && 
				countSquare(i, EMPTY, (byte)0))
			return true;
		return false;
	}
	
	private boolean is_edge(int i, int d)
	{
		if((square[i+LEFTUP]==EMPTY && square[i+RIGHTDOWN]==EMPTY) || 
				(square[i+LEFTDOWN]==EMPTY && square[i+RIGHTUP]==EMPTY))
			return false;
		else
		{
			byte x;
			if(d == 0 || d == 1)
				x = 2;
			else if(d == 2 || d == 3)
				x = 0;
			else
				return false;
			if(square[i + DIRECTION[x]] == WALL || square[i + DIRECTION[x + 1]] == WALL)
				return true;
			else
				return false;
		}
	}
	
	private void init_hash_code()
	{
		hash_code = 0;
		for(int i = 0; i != BOARD_SIZE; i++)
		{
			if(square[i] == MY_SIDE)
				hash_code ^= HashTable.SET_CODE[i][MY_SIDE];
			else if(square[i] == OP_SIDE)
				hash_code ^= HashTable.SET_CODE[i][OP_SIDE];
		}
	}
	
	public final void set_Board(byte[][] board, byte colSize, byte rowSize)
	{
		player = MY_SIDE;
		n_diffdiscs = 0;
		n_empties = 0;
		for(int i = BEGINPOINT; i != ENDPOINT; i--)
	    {
	    	int col = i%(colSize+1) - 1;
	    	int row = i/(colSize+1) - 1;
	    	if(col >= 0 && col < colSize && row >= 0 && row < rowSize)
	    	{
	    		if(board[col][row] == MY_SIDE + 1)
	    		{
	    			square[i] = MY_SIDE;
	    			n_diffdiscs++;
	    		}
	    		else if(board[col][row] == OP_SIDE + 1)
	    		{
	    			square[i] = OP_SIDE;
	    			n_diffdiscs--;
	    		}
	    		else if(board[col][row] == Xw213400.AVAILABLE && square[i] != WALL)
	    		{
	    			square[i] = EMPTY;
	    			n_empties++;
	    		}
	    	}
	    }
		set_edge();
		init_hash_code();
	}
	
	public final void flip_Board(Move move)
	{
		n_empties--;
		for(int i = 0; i != move.eat.size; i++)
		{
			square[move.eat.intNum[i]] = player;
			hash_code ^= HashTable.FLIP_CODE[move.eat.intNum[i]];
		}
		square[move.point] = player;
		hash_code ^= HashTable.SET_CODE[move.point][player];
		
		if(player == MY_SIDE)
			n_diffdiscs += (move.eat.size << 1) + 1;
		else
			n_diffdiscs -= (move.eat.size << 1) + 1;
		
		change_edge(move.point);
		
		player = (byte)(1 - player);
	}
		
	public final void untread_flip_Board(Move move)
	{
		n_empties++;
		for(int i = 0; i != move.eat.size; i++)
		{
			square[move.eat.intNum[i]] = player;
			hash_code ^= HashTable.FLIP_CODE[move.eat.intNum[i]];
		}
		square[move.point] = EMPTY;
		hash_code ^= HashTable.SET_CODE[move.point][1-player];
		
		if(player == MY_SIDE)
			n_diffdiscs += (move.eat.size << 1) + 1;
		else
			n_diffdiscs -= (move.eat.size << 1) + 1;
		
		untread_change_edge(move.point);
		
		player = (byte)(1 - player);
	}
	
	public final void flip_Board_no_edge(Move move)
	{
		n_empties--;
		for(int i = 0; i != move.eat.size; ++i)
		{
			square[move.eat.intNum[i]] = player;
			hash_code ^= HashTable.FLIP_CODE[move.eat.intNum[i]];
		}
		square[move.point] = player;
		hash_code ^= HashTable.SET_CODE[move.point][player];
		
		if(player == MY_SIDE)
			n_diffdiscs += (move.eat.size << 1) + 1;
		else
			n_diffdiscs -= (move.eat.size << 1) + 1;
		
		player = (byte)(1 - player);
	}

	public final void untread_flip_Board_no_edge(Move move)
	{
		n_empties++;
		for(int i = 0; i != move.eat.size; ++i)
		{
			square[move.eat.intNum[i]] = player;
			hash_code ^= HashTable.FLIP_CODE[move.eat.intNum[i]];
		}
		square[move.point] = EMPTY;
		hash_code ^= HashTable.SET_CODE[move.point][1-player];
		
		if(player == MY_SIDE)
			n_diffdiscs += (move.eat.size << 1) + 1;
		else
			n_diffdiscs -= (move.eat.size << 1) + 1;
		
		player = (byte)(1 - player);
	}
	
	public final void flip_Board_include_parity(Move move)
	{
		n_empties--;
		for(int i = 0; i != move.eat.size; i++)
		{
			square[move.eat.intNum[i]] = player;
			hash_code ^= HashTable.FLIP_CODE[move.eat.intNum[i]];
		}
		square[move.point] = player;
		hash_code ^= HashTable.SET_CODE[move.point][player];
		
		if(player == MY_SIDE)
			n_diffdiscs += (move.eat.size << 1) + 1;
		else
			n_diffdiscs -= (move.eat.size << 1) + 1;
		
		player = (byte)(1 - player);
		Hole.holePar[Hole.holeId[move.point]] = !Hole.holePar[Hole.holeId[move.point]];
	}

	public final void untread_flip_Board_include_parity(Move move)
	{
		n_empties++;
		for(int i = 0; i != move.eat.size; i++)
		{
			square[move.eat.intNum[i]] = player;
			hash_code ^= HashTable.FLIP_CODE[move.eat.intNum[i]];
		}
		square[move.point] = EMPTY;
		hash_code ^= HashTable.SET_CODE[move.point][1-player];
		
		if(player == MY_SIDE)
			n_diffdiscs += (move.eat.size << 1) + 1;
		else
			n_diffdiscs -= (move.eat.size << 1) + 1;
		
		player = (byte)(1 - player);
		Hole.holePar[Hole.holeId[move.point]] = !Hole.holePar[Hole.holeId[move.point]];
	}
	
	public final void flip_Board_no_parity(Move move)
	{
		n_empties--;
		for(int i = 0; i != move.eat.size; i++)
		{
			square[move.eat.intNum[i]] = player;
		}
		square[move.point] = player;
		
		if(player == MY_SIDE)
			n_diffdiscs += (move.eat.size << 1) + 1;
		else
			n_diffdiscs -= (move.eat.size << 1) + 1;
		
		player = (byte)(1 - player);
	}
	
	public final void untread_flip_Board_no_parity(Move move)
	{
		n_empties++;
		for(int i = 0; i != move.eat.size; i++)
		{
			square[move.eat.intNum[i]] = player;
		}
		square[move.point] = EMPTY;
		
		if(player == MY_SIDE)
			n_diffdiscs += (move.eat.size << 1) + 1;
		else
			n_diffdiscs -= (move.eat.size << 1) + 1;
		
		player = (byte)(1 - player);
	}
	
	public final boolean get_move(int pointIndex)
	{
		byte op = (byte)(1 - player);
		Xw213400.globle_tem_move.eat.size = 0;
		
		int point;
		short size;
		point = pointIndex + DIRECTION[0];
		if(square[point] == op)
		{
			size = Xw213400.globle_tem_move.eat.size;
			Xw213400.globle_tem_move.eat.add(point);
			for(point += DIRECTION[0]; square[point] == op; point += DIRECTION[0])
			{
				Xw213400.globle_tem_move.eat.add(point);
			}
			if(square[point] != player)
			{
				Xw213400.globle_tem_move.eat.size = size;
			}
		}
		point = pointIndex + DIRECTION[1];
		if(square[point] == op)
		{
			size = Xw213400.globle_tem_move.eat.size;
			Xw213400.globle_tem_move.eat.add(point);
			for(point += DIRECTION[1]; square[point] == op; point += DIRECTION[1])
			{
				Xw213400.globle_tem_move.eat.add(point);
			}
			if(square[point] != player)
			{
				Xw213400.globle_tem_move.eat.size = size;
			}
		}
		point = pointIndex + DIRECTION[2];
		if(square[point] == op)
		{
			size = Xw213400.globle_tem_move.eat.size;
			Xw213400.globle_tem_move.eat.add(point);
			for(point += DIRECTION[2]; square[point] == op; point += DIRECTION[2])
			{
				Xw213400.globle_tem_move.eat.add(point);
			}
			if(square[point] != player)
			{
				Xw213400.globle_tem_move.eat.size = size;
			}
		}
		point = pointIndex + DIRECTION[3];
		if(square[point] == op)
		{
			size = Xw213400.globle_tem_move.eat.size;
			Xw213400.globle_tem_move.eat.add(point);
			for(point += DIRECTION[3]; square[point] == op; point += DIRECTION[3])
			{
				Xw213400.globle_tem_move.eat.add(point);
			}
			if(square[point] != player)
			{
				Xw213400.globle_tem_move.eat.size = size;
			}
		}
		point = pointIndex + DIRECTION[4];
		if(square[point] == op)
		{
			size = Xw213400.globle_tem_move.eat.size;
			Xw213400.globle_tem_move.eat.add(point);
			for(point += DIRECTION[4]; square[point] == op; point += DIRECTION[4])
			{
				Xw213400.globle_tem_move.eat.add(point);
			}
			if(square[point] != player)
			{
				Xw213400.globle_tem_move.eat.size = size;
			}
		}
		point = pointIndex + DIRECTION[5];
		if(square[point] == op)
		{
			size = Xw213400.globle_tem_move.eat.size;
			Xw213400.globle_tem_move.eat.add(point);
			for(point += DIRECTION[5]; square[point] == op; point += DIRECTION[5])
			{
				Xw213400.globle_tem_move.eat.add(point);
			}
			if(square[point] != player)
			{
				Xw213400.globle_tem_move.eat.size = size;
			}
		}
		point = pointIndex + DIRECTION[6];
		if(square[point] == op)
		{
			size = Xw213400.globle_tem_move.eat.size;
			Xw213400.globle_tem_move.eat.add(point);
			for(point += DIRECTION[6]; square[point] == op; point += DIRECTION[6])
			{
				Xw213400.globle_tem_move.eat.add(point);
			}
			if(square[point] != player)
			{
				Xw213400.globle_tem_move.eat.size = size;
			}
		}
		point = pointIndex + DIRECTION[7];
		if(square[point] == op)
		{
			size = Xw213400.globle_tem_move.eat.size;
			Xw213400.globle_tem_move.eat.add(point);
			for(point += DIRECTION[7]; square[point] == op; point += DIRECTION[7])
			{
				Xw213400.globle_tem_move.eat.add(point);
			}
			if(square[point] != player)
			{
				Xw213400.globle_tem_move.eat.size = size;
			}
		}
		
		if(Xw213400.globle_tem_move.eat.size != 0)
		{
			Xw213400.globle_tem_move.point = pointIndex;
			return true;
		}
		return false;
	}
	
	private final int mobilityScore(int mob, int pot)
	{
		int mobility = 0;
		for(int index = 0; index != TOTAL_SQUARE; index++)
		{
			if(!edge[PRESORT.square[index]])
				continue;
			boolean mob_my = false, mob_op = false, pot_my = false, pot_op = false;			
			int point;
			
			point = PRESORT.square[index] + DIRECTION[0];
			if(square[point] == OP_SIDE || square[point] == MY_SIDE)
			{
				for(; square[point] == square[point + DIRECTION[0]]; point += DIRECTION[0])
				{}
				if(square[point + DIRECTION[0]] == MY_SIDE)
					mob_my = true;
				else if(square[point + DIRECTION[0]] == OP_SIDE)
					mob_op = true;
				else if(square[point + DIRECTION[0]] == EMPTY)
				{
					if(square[point] == OP_SIDE)
						pot_my = true;
					else if(square[point] == MY_SIDE)
						pot_op = true;
				}
			}
			point = PRESORT.square[index] + DIRECTION[1];
			if(square[point] == OP_SIDE || square[point] == MY_SIDE)
			{
				for(; square[point] == square[point + DIRECTION[1]]; point += DIRECTION[1])
				{}
				if(square[point + DIRECTION[1]] == MY_SIDE)
					mob_my = true;
				else if(square[point + DIRECTION[1]] == OP_SIDE)
					mob_op = true;
				else if(square[point + DIRECTION[1]] == EMPTY)
				{
					if(square[point] == OP_SIDE)
						pot_my = true;
					else if(square[point] == MY_SIDE)
						pot_op = true;
				}
			}
			point = PRESORT.square[index] + DIRECTION[2];
			if(square[point] == OP_SIDE || square[point] == MY_SIDE)
			{
				for(; square[point] == square[point + DIRECTION[2]]; point += DIRECTION[2])
				{}
				if(square[point + DIRECTION[2]] == MY_SIDE)
					mob_my = true;
				else if(square[point + DIRECTION[2]] == OP_SIDE)
					mob_op = true;
				else if(square[point + DIRECTION[2]] == EMPTY)
				{
					if(square[point] == OP_SIDE)
						pot_my = true;
					else if(square[point] == MY_SIDE)
						pot_op = true;
				}
			}
			point = PRESORT.square[index] + DIRECTION[3];
			if(square[point] == OP_SIDE || square[point] == MY_SIDE)
			{
				for(; square[point] == square[point + DIRECTION[3]]; point += DIRECTION[3])
				{}
				if(square[point + DIRECTION[3]] == MY_SIDE)
					mob_my = true;
				else if(square[point + DIRECTION[3]] == OP_SIDE)
					mob_op = true;
				else if(square[point + DIRECTION[3]] == EMPTY)
				{
					if(square[point] == OP_SIDE)
						pot_my = true;
					else if(square[point] == MY_SIDE)
						pot_op = true;
				}
			}
			point = PRESORT.square[index] + DIRECTION[4];
			if(square[point] == OP_SIDE || square[point] == MY_SIDE)
			{
				for(; square[point] == square[point + DIRECTION[4]]; point += DIRECTION[4])
				{}
				if(square[point + DIRECTION[4]] == MY_SIDE)
					mob_my = true;
				else if(square[point + DIRECTION[4]] == OP_SIDE)
					mob_op = true;
				else if(square[point + DIRECTION[4]] == EMPTY)
				{
					if(square[point] == OP_SIDE)
						pot_my = true;
					else if(square[point] == MY_SIDE)
						pot_op = true;
				}
			}
			point = PRESORT.square[index] + DIRECTION[5];
			if(square[point] == OP_SIDE || square[point] == MY_SIDE)
			{
				for(; square[point] == square[point + DIRECTION[5]]; point += DIRECTION[5])
				{}
				if(square[point + DIRECTION[5]] == MY_SIDE)
					mob_my = true;
				else if(square[point + DIRECTION[5]] == OP_SIDE)
					mob_op = true;
				else if(square[point + DIRECTION[5]] == EMPTY)
				{
					if(square[point] == OP_SIDE)
						pot_my = true;
					else if(square[point] == MY_SIDE)
						pot_op = true;
				}
			}
			point = PRESORT.square[index] + DIRECTION[6];
			if(square[point] == OP_SIDE || square[point] == MY_SIDE)
			{
				for(; square[point] == square[point + DIRECTION[6]]; point += DIRECTION[6])
				{}
				if(square[point + DIRECTION[6]] == MY_SIDE)
					mob_my = true;
				else if(square[point + DIRECTION[6]] == OP_SIDE)
					mob_op = true;
				else if(square[point + DIRECTION[6]] == EMPTY)
				{
					if(square[point] == OP_SIDE)
						pot_my = true;
					else if(square[point] == MY_SIDE)
						pot_op = true;
				}
			}
			point = PRESORT.square[index] + DIRECTION[7];
			if(square[point] == OP_SIDE || square[point] == MY_SIDE)
			{
				for(; square[point] == square[point + DIRECTION[7]]; point += DIRECTION[7])
				{}
				if(square[point + DIRECTION[7]] == MY_SIDE)
					mob_my = true;
				else if(square[point + DIRECTION[7]] == OP_SIDE)
					mob_op = true;
				else if(square[point + DIRECTION[7]] == EMPTY)
				{
					if(square[point] == OP_SIDE)
						pot_my = true;
					else if(square[point] == MY_SIDE)
						pot_op = true;
				}
			}
			
			if(mob_my)
				mobility += mob;
			if(pot_my)
				mobility += pot;
			if(mob_op)
				mobility -= mob;
			if(pot_op)
				mobility -= pot;
		}
		
		return mobility;
	}
	
	public final int mobility_fastfirst(byte p)
	{
		int mobility = 0, point;
		byte o = (byte)(1 - p);
		for(int index = 0; index != empties.size; index++)
		{
			if(square[empties.intNum[index]] == EMPTY)
			{
				point = empties.intNum[index] + DIRECTION[0];
				if(square[point] == o)
				{
					for(point += DIRECTION[0]; square[point] == o; point += DIRECTION[0])
					{}
					if(square[point] == p)
						mobility++;
					else
					{
						point = empties.intNum[index] + DIRECTION[1];
						if(square[point] == o)
						{
							for(point += DIRECTION[1]; square[point] == o; point += DIRECTION[1])
							{}
							if(square[point] == p)
								mobility++;
							else
							{
								point = empties.intNum[index] + DIRECTION[2];
								if(square[point] == o)
								{
									for(point += DIRECTION[2]; square[point] == o; point += DIRECTION[2])
									{}
									if(square[point] == p)
										mobility++;
									else
									{
										point = empties.intNum[index] + DIRECTION[3];
										if(square[point] == o)
										{
											for(point += DIRECTION[3]; square[point] == o; point += DIRECTION[3])
											{}
											if(square[point] == p)
												mobility++;
											else
											{
												point = empties.intNum[index] + DIRECTION[4];
												if(square[point] == o)
												{
													for(point += DIRECTION[4]; square[point] == o; point += DIRECTION[4])
													{}
													if(square[point] == p)
														mobility++;
													else
													{
														point = empties.intNum[index] + DIRECTION[5];
														if(square[point] == o)
														{
															for(point += DIRECTION[5]; square[point] == o; point += DIRECTION[5])
															{}
															if(square[point] == p)
																mobility++;
															else
															{
																point = empties.intNum[index] + DIRECTION[6];
																if(square[point] == o)
																{
																	for(point += DIRECTION[6]; square[point] == o; point += DIRECTION[6])
																	{}
																	if(square[point] == p)
																		mobility++;
																	else
																	{
																		point = empties.intNum[index] + DIRECTION[7];
																		if(square[point] == o)
																		{
																			for(point += DIRECTION[7]; square[point] == o; point += DIRECTION[7])
																			{}
																			if(square[point] == p)
																				mobility++;
																		}
																	}
																}
															}
														}
													}
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
		return mobility;
	}
	
	public final void set_empties()
	{
		empties.size = 0;
		for(int i = 0; i != TOTAL_SQUARE; i++)
		{
			if(square[PRESORT.square[i]] == EMPTY)
				empties.add(PRESORT.square[i]);
		}
	}
	
	public final int evaluate()
	{
		int score = 0;

		if(Xw213400.currStep <= STEP1)
		{
			if(Xw213400.currStep <= 16)
				score = cornerScore(30, 0, 20) + mobilityScore(38, 12) - n_diffdiscs;
			else
				score = cornerScore(27, 0, 18) + mobilityScore(39, 11);
		}
		else if(Xw213400.currStep <= STEP2)
		{
			score = cornerScore(24, 30, 16) + mobilityScore(40, 10);
		}
		else
		{
			if(n_empties > 16)
				score = cornerScore(18, 18, 12) + stableScore() + mobilityScore(41, 9);
			else if(n_empties > 12)
				score = cornerScore(12, 12, 8) + stableScore() + mobilityScore(42, 8);
			else
				score = cornerScore(3, 3, 2) + stableScore() + mobilityScore(43, 7);
		}
		
		return score;
	}
	
	private final int cornerScore(int co, int s, int x)
	{
		int score = 0, size = CORNER.size();
		for(int i = 0; i != size; i++)
		{
			Corner corner = CORNER.get(i);
			if(corner.eff_corner != 0)
			{
				if(square[corner.position] == MY_SIDE)
				{
					score += co * corner.eff_corner;
					if(s != 0)
					{
						int index;
						if(corner.neighbor[0] == true)
						{
							index = corner.position + DIRECTION[0];
							for( ; square[index] == MY_SIDE; index += Board.DIRECTION[0])
							{
								score += s;
							}
							if((index - corner.position) == (corner.effection[0] + 1) * Board.DIRECTION[0])
								score -= (s * corner.effection[0]) >> 1;
						}
						if(corner.neighbor[1] == true)
						{
							index = corner.position + DIRECTION[1];
							for( ; square[index] == MY_SIDE; index += Board.DIRECTION[1])
							{
								score += s;
							}
							if((index - corner.position) == (corner.effection[1] + 1) * Board.DIRECTION[1])
								score -= (s * corner.effection[1]) >> 1;
						}
						if(corner.neighbor[2] == true)
						{
							index = corner.position + DIRECTION[2];
							for( ; square[index] == MY_SIDE; index += Board.DIRECTION[2])
							{
								score += s;
							}
							if((index - corner.position) == (corner.effection[2] + 1) * Board.DIRECTION[2])
								score -= (s * corner.effection[2]) >> 1;
						}
						if(corner.neighbor[3] == true)
						{
							index = corner.position + DIRECTION[3];
							for( ; square[index] == MY_SIDE; index += Board.DIRECTION[3])
							{
								score += s;
							}
							if((index - corner.position) == (corner.effection[3] + 1) * Board.DIRECTION[3])
								score -= (s * corner.effection[3]) >> 1;
						}
					}
				}
				else if(square[corner.position] == OP_SIDE)
				{
					score -= co * corner.eff_corner;
					if(s != 0)
					{
						int index;
						if(corner.neighbor[0] == true)
						{
							index = corner.position + DIRECTION[0];
							for( ; square[index] == OP_SIDE; index += Board.DIRECTION[0])
							{
								score -= s;
							}
							if((index - corner.position) == (corner.effection[0] + 1) * Board.DIRECTION[0])
								score += (s * corner.effection[0]) >> 1;
						}
						if(corner.neighbor[1] == true)
						{
							index = corner.position + DIRECTION[1];
							for( ; square[index] == OP_SIDE; index += Board.DIRECTION[1])
							{
								score -= s;
							}
							if((index - corner.position) == (corner.effection[1] + 1) * Board.DIRECTION[1])
								score += (s * corner.effection[1]) >> 1;
						}
						if(corner.neighbor[2] == true)
						{
							index = corner.position + DIRECTION[2];
							for( ; square[index] == OP_SIDE; index += Board.DIRECTION[2])
							{
								score -= s;
							}
							if((index - corner.position) == (corner.effection[2] + 1) * Board.DIRECTION[2])
								score += (s * corner.effection[2]) >> 1;
						}
						if(corner.neighbor[3] == true)
						{
							index = corner.position + DIRECTION[3];
							for( ; square[index] == OP_SIDE; index += Board.DIRECTION[3])
							{
								score -= s;
							}
							if((index - corner.position) == (corner.effection[3] + 1) * Board.DIRECTION[3])
								score += (s * corner.effection[3]) >> 1;
						}
					}
				}
				else
				{
					if(x != 0)
					{
						for(int j = 0; j != corner.xSquare.size; j++)
						{
							if(Board.square[corner.xSquare.intNum[j]] == MY_SIDE)
								score -= corner.eff_corner * x;
							else if(square[corner.xSquare.intNum[j]] == OP_SIDE)
								score += corner.eff_corner * x;
						}
					}
				}
			}
			else
			{
				if(s != 0)
				{
					if(square[corner.position] == MY_SIDE)
						score += s;
					else if(square[corner.position] == OP_SIDE)
						score -= s;
					else
					{
						if(corner.xSquare.size != 0)
						{
							if(square[corner.xSquare.intNum[0]] == MY_SIDE)
								score -= s >> 1;
							else if(square[corner.xSquare.intNum[0]] == OP_SIDE)
								score += s >> 1;
							if(corner.xSquare.size != 1)
							{
								if(square[corner.xSquare.intNum[1]] == MY_SIDE)
									score -= s >> 1;
								else if(square[corner.xSquare.intNum[1]] == OP_SIDE)
									score += s >> 1;
				                if(corner.xSquare.size != 2)
				                {
				    				if(square[corner.xSquare.intNum[2]] == MY_SIDE)
				    					score -= s >> 1;
				    				else if(square[corner.xSquare.intNum[2]] == OP_SIDE)
				    					score += s >> 1;
				    			}
							}
						}
					}
				}
			}
		}
		return score;
	}
	
	private final int stableScore()
	{
		int score = 0, size = CORNER.size();
		boolean empty, exchange;
		for(int i = BEGINPOINT; i != ENDPOINT; i--)
		{
			if(square[i] == EMPTY || square[i] == WALL)
				tem_array[i] = 0;
			else
				tem_array[i] = 1;
		}
		
		for(int i = 0; i != size; i++)
		{
			Corner corner = CORNER.get(i);
			if(corner.eff_corner != 0)
			{
				if(square[corner.position] == MY_SIDE)
				{
					score += 16;
					tem_array[corner.position] = 0;
					int index;
					if(corner.neighbor[0] == true)
					{
						index = corner.position + DIRECTION[0];
						for( ; square[index] == MY_SIDE; index += Board.DIRECTION[0])
						{
							score += 16;
							tem_array[index] = 0;
						}
						if((index - corner.position) == (corner.effection[0] + 1) * Board.DIRECTION[0])
							score -= corner.effection[0] << 3;
					}
					if(corner.neighbor[1] == true)
					{
						index = corner.position + DIRECTION[1];
						for( ; square[index] == MY_SIDE; index += Board.DIRECTION[1])
						{
							score += 16;
							tem_array[index] = 0;
						}
						if((index - corner.position) == (corner.effection[1] + 1) * Board.DIRECTION[1])
							score -= corner.effection[1] << 3;
					}
					if(corner.neighbor[2] == true)
					{
						index = corner.position + DIRECTION[2];
						for( ; square[index] == MY_SIDE; index += Board.DIRECTION[2])
						{
							score += 16;
							tem_array[index] = 0;
						}
						if((index - corner.position) == (corner.effection[2] + 1) * Board.DIRECTION[2])
							score -= corner.effection[2] << 3;
					}
					if(corner.neighbor[3] == true)
					{
						index = corner.position + DIRECTION[3];
						for( ; square[index] == MY_SIDE; index += Board.DIRECTION[3])
						{
							score += 16;
							tem_array[index] = 0;
						}
						if((index - corner.position) == (corner.effection[3] + 1) * Board.DIRECTION[3])
							score -= corner.effection[3] << 3;
					}
				}
				else if(square[corner.position] == OP_SIDE)
				{
					score -= 16;
					tem_array[corner.position] = 0;
					int index;
					if(corner.neighbor[0] == true)
					{
						index = corner.position + DIRECTION[0];
						for( ; square[index] == OP_SIDE; index += Board.DIRECTION[0])
						{
							score -= 16;
							tem_array[index] = 0;
						}
						if((index - corner.position) == (corner.effection[0] + 1) * Board.DIRECTION[0])
							score += corner.effection[0] << 3;
					}
					if(corner.neighbor[1] == true)
					{
						index = corner.position + DIRECTION[1];
						for( ; square[index] == OP_SIDE; index += Board.DIRECTION[1])
						{
							score -= 16;
							tem_array[index] = 0;
						}
						if((index - corner.position) == (corner.effection[1] + 1) * Board.DIRECTION[1])
							score += corner.effection[1] << 3;
					}
					if(corner.neighbor[2] == true)
					{
						index = corner.position + DIRECTION[2];
						for( ; square[index] == OP_SIDE; index += Board.DIRECTION[2])
						{
							score -= 16;
							tem_array[index] = 0;
						}
						if((index - corner.position) == (corner.effection[2] + 1) * Board.DIRECTION[2])
							score += corner.effection[2] << 3;
					}
					if(corner.neighbor[3] == true)
					{
						index = corner.position + DIRECTION[3];
						for( ; square[index] == OP_SIDE; index += Board.DIRECTION[3])
						{
							score -= 16;
							tem_array[index] = 0;
						}
						if((index - corner.position) == (corner.effection[3] + 1) * Board.DIRECTION[3])
							score += corner.effection[3] << 3;
					}
				}
			}
			else
			{
				if(square[corner.position] == MY_SIDE)
				{
					score += 16;
					tem_array[corner.position] = 0;
				}
				else if(square[corner.position] == OP_SIDE)
				{
					score -= 16;
					tem_array[corner.position] = 0;
				}
			}
		}
		
		for(int i = BEGINPOINT; i != ENDPOINT; i--)
		{
			if(tem_array[i] == 0)
				continue;
			else
			{
				for(int j = 0; j != 8; j += 2)
				{
					if(tem_array[i] != 0)
					{
						empty = false; exchange = false;
						for(int k = i + DIRECTION[j]; square[k] != WALL; k += DIRECTION[j])
						{
							if(square[k] == EMPTY)
							{
								empty = true;
								break;
							}
							if(square[k] != square[i])
								exchange = true;
						}
						if(empty)
						{
							if(!exchange)
							{
								empty = false; exchange = false;
								for(int k = i - DIRECTION[j]; square[k] != WALL; k -= DIRECTION[j])
								{
									if(square[k] == EMPTY)
									{
										empty = true;
										break;
									}
									if(square[k] != square[i])
										exchange = true;
								}
								if(!empty && !exchange)
									tem_array[i] <<= 1;
								else
									tem_array[i] = 0;
								if(exchange)
								{
									for(int k = i - DIRECTION[j]; square[k] != WALL; k -= DIRECTION[j])
									{
										if(square[k] == square[i])
											tem_array[k] = 0;
										else
											break;
									}
								}
							}
							else
							{
								empty = false; exchange = false;
								for(int k = i - DIRECTION[j]; square[k] != WALL; k -= DIRECTION[j])
								{
									if(square[k] == EMPTY)
									{
										empty = true;
										break;
									}
									if(square[k] != square[i])
										exchange = true;
								}
								if(empty && !exchange)
									tem_array[i] = 0;
								else if(!empty && !exchange)
									tem_array[i] <<= 1;
							}
						}
						else
						{
							if(exchange)
							{
								empty = false; exchange = false;
								for(int k = i - DIRECTION[j]; square[k] != WALL; k -= DIRECTION[j])
								{
									if(square[k] == EMPTY)
									{
										empty = true;
										break;
									}
									if(square[k] != square[i])
										exchange = true;
								}
								if(!empty)
									tem_array[i] <<= 1;
								else
								{
									if(!exchange)
										tem_array[i] = 0;
								}	
							}
							else
								tem_array[i] <<= 1;
						}
					}
					else
						break;
				}
			}
		}
		for(int i = BEGINPOINT; i != ENDPOINT; i--)
		{
			if(square[i] == MY_SIDE)
				score += tem_array[i];
			else if(square[i] == OP_SIDE)
				score -= tem_array[i];
		}
		return score;
	}
	
	private final void change_edge(int point)
	{
		edge[point] = false;
		if(square[point + DIRECTION[0]] == EMPTY)
			edge[point + DIRECTION[0]] = true;
		if(square[point + DIRECTION[1]] == EMPTY)
			edge[point + DIRECTION[1]] = true;
		if(square[point + DIRECTION[2]] == EMPTY)
			edge[point + DIRECTION[2]] = true;
		if(square[point + DIRECTION[3]] == EMPTY)
			edge[point + DIRECTION[3]] = true;
		if(square[point + DIRECTION[4]] == EMPTY)
			edge[point + DIRECTION[4]] = true;
		if(square[point + DIRECTION[5]] == EMPTY)
			edge[point + DIRECTION[5]] = true;
		if(square[point + DIRECTION[6]] == EMPTY)
			edge[point + DIRECTION[6]] = true;
		if(square[point + DIRECTION[7]] == EMPTY)
			edge[point + DIRECTION[7]] = true;
	}
	
	private final void untread_change_edge(int point)
	{
		edge[point] = true;
		if(square[point + DIRECTION[0]] == EMPTY && !countSquare(point + DIRECTION[0], MY_SIDE, (byte)0) && 
				!countSquare(point + DIRECTION[0], OP_SIDE, (byte)0))
		{
			edge[point + DIRECTION[0]] = false;
		}
		if(square[point + DIRECTION[1]] == EMPTY && !countSquare(point + DIRECTION[1], MY_SIDE, (byte)0) && 
				!countSquare(point + DIRECTION[1], OP_SIDE, (byte)0))
		{
			edge[point + DIRECTION[1]] = false;
		}
		if(square[point + DIRECTION[2]] == EMPTY && !countSquare(point + DIRECTION[2], MY_SIDE, (byte)0) && 
				!countSquare(point + DIRECTION[2], OP_SIDE, (byte)0))
		{
			edge[point + DIRECTION[2]] = false;
		}
		if(square[point + DIRECTION[3]] == EMPTY && !countSquare(point + DIRECTION[3], MY_SIDE, (byte)0) && 
				!countSquare(point + DIRECTION[3], OP_SIDE, (byte)0))
		{
			edge[point + DIRECTION[3]] = false;
		}
		if(square[point + DIRECTION[4]] == EMPTY && !countSquare(point + DIRECTION[4], MY_SIDE, (byte)0) && 
				!countSquare(point + DIRECTION[4], OP_SIDE, (byte)0))
		{
			edge[point + DIRECTION[4]] = false;
		}
		if(square[point + DIRECTION[5]] == EMPTY && !countSquare(point + DIRECTION[5], MY_SIDE, (byte)0) && 
				!countSquare(point + DIRECTION[5], OP_SIDE, (byte)0))
		{
			edge[point + DIRECTION[5]] = false;
		}
		if(square[point + DIRECTION[6]] == EMPTY && !countSquare(point + DIRECTION[6], MY_SIDE, (byte)0) && 
				!countSquare(point + DIRECTION[6], OP_SIDE, (byte)0))
		{
			edge[point + DIRECTION[6]] = false;
		}
		if(square[point + DIRECTION[7]] == EMPTY && !countSquare(point + DIRECTION[7], MY_SIDE, (byte)0) && 
				!countSquare(point + DIRECTION[7], OP_SIDE, (byte)0))
		{
			edge[point + DIRECTION[7]] = false;
		}
	}
}
