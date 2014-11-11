void main()
{

  int i;
  int j;

  i = j;

  while(j < 100)
  {
    if (j % 2 == 0)
      i = i + 2;
    j = j + 1;
  }
  assert(i == j);

}
