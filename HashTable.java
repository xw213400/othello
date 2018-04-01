package xw213400;

import java.util.HashMap;
import java.util.Random;

class HashTable
{
	static HashMap<Long, Hash> transTable;
	static long[][] SET_CODE;
	static long[] FLIP_CODE;
	static long SWAP;
	
	HashTable(int size)
	{
		SET_CODE = new long[size][2];
		FLIP_CODE = new long[size];
		transTable = new HashMap<Long, Hash>(0x2000, (float)0.7);
		java.util.Random random = new Random();
		SWAP = random.nextLong();
		for(int i = 0; i != size; ++i)
		{
			SET_CODE[i][0] = random.nextLong();
			SET_CODE[i][1] = random.nextLong();
			FLIP_CODE[i] = SET_CODE[i][0] ^ SET_CODE[i][1];
			SET_CODE[i][0] ^= SWAP;
			SET_CODE[i][1] ^= SWAP;
		}
	}

	final void add(int alpha, int beta, int score)
	{
		if(!transTable.containsKey(Board.hash_code))
		{
			transTable.put(Board.hash_code, new Hash(-1000000, 1000000));
			if(score > alpha)
				transTable.get(Board.hash_code).lower = score;
			if(score < beta)
				transTable.get(Board.hash_code).upper = score;		
		}
		else
		{
			if(score < beta && score < transTable.get(Board.hash_code).upper)
				transTable.get(Board.hash_code).upper = score;
			if(score > alpha && score > transTable.get(Board.hash_code).lower)
				transTable.get(Board.hash_code).lower = score;
		}
	}
}
