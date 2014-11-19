
void main(int x, int y) {
  x = y; 
  assume(x <= 10);

  int i;
  int j;
  int a;
  int b;
  i = 0;

  while(i < 100) {
    j = 0;    
    a = 200;
    b = 0;

    while(j < 100) {
      a = a - 1;
      b = b + 1;

      j = j + 1;
    }

    x = x + a;
    y = y + b;

    i = i + 1;
  }
  
  assert(x == y);
}
