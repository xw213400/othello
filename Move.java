package xw213400;

class Move
{
	int point;
	IntArray eat;

	Move()
	{
		point = -1;
		eat = new IntArray(Board.MAX_FLIPS);
	}
	
	void copy(Move move)
	{
		point = move.point;
		eat.copy(move.eat);
	}
}
