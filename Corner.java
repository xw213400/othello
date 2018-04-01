package xw213400;

class Corner 
{
	int position;
	int eff_corner;
	boolean neighbor[];
	byte effection[];
	IntArray cSquare;
	IntArray xSquare;
	
	Corner()
	{
		position = -1;
		eff_corner = 0;
		neighbor = new boolean[4];
		effection = new byte[4];
		cSquare = new IntArray(2);
		xSquare = new IntArray(3);
		neighbor[0] = false;
		neighbor[1] = false;
		neighbor[2] = false;
		neighbor[3] = false;
		effection[0] = 0;
		effection[1] = 0;
		effection[2] = 0;
		effection[3] = 0;
	}
}
