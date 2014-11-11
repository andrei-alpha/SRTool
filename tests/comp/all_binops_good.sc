void main(int z, int x)
{
  assume(z < 1000 && x < 1000 && z > 0 && x > 0);
  int remainder;
  remainder = z%x;
  int division;
  division = z/x;

  assert ( z == division*x+remainder);

  int k;
  k = (z << 1) >> 1;
  assert(k == z);

  assert(x^x == 0);
  assert(!(x|!x) == 0);
  assert((x&!x) == 0);
  
  assert(!(1&&0));
  assert(1||1);
  assert(x >= x);
  assert(x >= x-1);
  assert(x <= x);
  assert(x <= x+1);
  assert(x < x+1);
  assert(x > x-1);
  assert(x == x);
  assert(x != x-1);

  int i;
  i = 1;
  int j;
  j = 2;
  i = ((i - 1 + 2)*4)/(-1);
  j = 2 - 10;
  assert(i==j);
}
