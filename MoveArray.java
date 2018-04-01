package xw213400;

class MoveArray
{
	Move[] moves;
	short size;

	MoveArray(int length)
	{
		moves = new Move[length];
		for(int i = 0; i != length; i++)
		{
			moves[i] = new Move();
		}
		size = 0;
	}
	
	final void add(Move move)
	{
		moves[size].copy(move);
		size++;
	}
	
	final void insert(Move move, short location)
	{
		moves[size].copy(move);
		Move tem_move = moves[size];
		for(int i = size; i != location; i--)
		{
			moves[i] = moves[i-1];
		}
		moves[location] = tem_move;
		size++;
	}
}
