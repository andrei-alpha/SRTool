
void main(int a)
{
	int i;
	int j;
	
	i=1;
	j=a;
	
	i = i + 1;
	
	j = j + i;
	
	assert(j == a + 2);
}
