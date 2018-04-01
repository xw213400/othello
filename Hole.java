package xw213400;

class Hole 
{
	static IntArray hole;
	static boolean[] holePar;
	static byte[] holeId;
	static byte id;
	
	private IntArray tem_hole1;
	private IntArray tem_hole2;
	
	Hole()
	{
		hole = new IntArray(7);
		tem_hole1 = new IntArray(7);
		tem_hole2 = new IntArray(7);
		holePar = new boolean[7];
		holeId = new byte[Board.BOARD_SIZE];
		for(int i = Board.BEGINPOINT; i != Board.ENDPOINT; i--)
		{
			Hole.holeId[i] = 8;
		}
	}
	
	public void init_Hole()
	{
		Hole.holeId[hole.intNum[0]] = 8;
		Hole.holeId[hole.intNum[1]] = 8;
		Hole.holeId[hole.intNum[2]] = 8;
		Hole.holeId[hole.intNum[3]] = 8;
		Hole.holeId[hole.intNum[4]] = 8;
		Hole.holeId[hole.intNum[5]] = 8;
		Hole.holeId[hole.intNum[6]] = 8;
		Hole.id = 0;
		Hole.hole.size = 0;
		for(int index = 0; index != Board.empties.size; ++index)
		{
			if(Board.square[Board.empties.intNum[index]] == Board.EMPTY && holeId[Board.empties.intNum[index]] == 8)
			{
				int point = Board.empties.intNum[index];
				tem_hole1.size = 0;
				tem_hole1.add(point);
				holeId[point] = -1;
				tem_hole2.size = 0;
				for(;;)
				{
					if(Board.square[point + Board.DIRECTION[0]] == Board.EMPTY && holeId[point + Board.DIRECTION[0]] == 8)
					{
						tem_hole1.add(point + Board.DIRECTION[0]);
						holeId[point + Board.DIRECTION[0]] = -1;
					}
					if(Board.square[point + Board.DIRECTION[1]] == Board.EMPTY && holeId[point + Board.DIRECTION[1]] == 8)
					{
						tem_hole1.add(point + Board.DIRECTION[1]);
						holeId[point + Board.DIRECTION[1]] = -1;
					}
					if(Board.square[point + Board.DIRECTION[2]] == Board.EMPTY && holeId[point + Board.DIRECTION[2]] == 8)
					{
						tem_hole1.add(point + Board.DIRECTION[2]);
						holeId[point + Board.DIRECTION[2]] = -1;
					}
					if(Board.square[point + Board.DIRECTION[3]] == Board.EMPTY && holeId[point + Board.DIRECTION[3]] == 8)
					{
						tem_hole1.add(point + Board.DIRECTION[3]);
						holeId[point + Board.DIRECTION[3]] = -1;
					}
					if(Board.square[point + Board.DIRECTION[4]] == Board.EMPTY && holeId[point + Board.DIRECTION[4]] == 8)
					{
						tem_hole1.add(point + Board.DIRECTION[4]);
						holeId[point + Board.DIRECTION[4]] = -1;
					}
					if(Board.square[point + Board.DIRECTION[5]] == Board.EMPTY && holeId[point + Board.DIRECTION[5]] == 8)
					{
						tem_hole1.add(point + Board.DIRECTION[5]);
						holeId[point + Board.DIRECTION[5]] = -1;
					}
					if(Board.square[point + Board.DIRECTION[6]] == Board.EMPTY && holeId[point + Board.DIRECTION[6]] == 8)
					{
						tem_hole1.add(point + Board.DIRECTION[6]);
						holeId[point + Board.DIRECTION[6]] = -1;
					}
					if(Board.square[point + Board.DIRECTION[7]] == Board.EMPTY && holeId[point + Board.DIRECTION[7]] == 8)
					{
						tem_hole1.add(point + Board.DIRECTION[7]);
						holeId[point + Board.DIRECTION[7]] = -1;
					}
					tem_hole1.size--;
					point = tem_hole1.intNum[tem_hole1.size];
					tem_hole2.add(point);
					if(tem_hole1.size == 0)
						break;
				}
				if(tem_hole2.size != 0)
				{
					hole.add(tem_hole2.intNum[0]);
					holeId[tem_hole2.intNum[0]] = id;
					if(tem_hole2.size != 1)
					{
						hole.add(tem_hole2.intNum[1]);
						holeId[tem_hole2.intNum[1]] = id;
						if(tem_hole2.size != 2)
						{
							hole.add(tem_hole2.intNum[2]);
							holeId[tem_hole2.intNum[2]] = id;
							if(tem_hole2.size != 3)
							{
								hole.add(tem_hole2.intNum[3]);
								holeId[tem_hole2.intNum[3]] = id;
								if(tem_hole2.size != 4)
								{
									hole.add(tem_hole2.intNum[4]);
									holeId[tem_hole2.intNum[4]] = id;
									if(tem_hole2.size != 5)
									{
										hole.add(tem_hole2.intNum[5]);
										holeId[tem_hole2.intNum[5]] = id;
										if(tem_hole2.size != 6)
										{
											hole.add(tem_hole2.intNum[6]);
											holeId[tem_hole2.intNum[6]] = id;
										}
									}
								}
							}
						}
					}
				}
				holePar[id] = tem_hole2.size%2 == 1 ? true : false;
				id++;
			}
		}
	}
}
