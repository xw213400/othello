package xw213400;

class IntArray
{
	int[] intNum;
	short size;

	IntArray(int length)
	{
		intNum = new int[length];
		size = 0;
	}
	
	final void copy(IntArray array)
	{
		for(int i = 0; i != array.size; i++)
		{
			intNum[i] = array.intNum[i];
		}
		size = array.size;
	}
	
	final void add(int value)
	{
		intNum[size] = value;
		size++;
	}
	
	final void insert(int value, short location)
	{
		for(int i = size; i != location; i--)
		{
			intNum[i] = intNum[i-1];
		}
		intNum[location] = value;
		size++;
	}
}
