
void main(int x, int y) {
  int g;

  if (y > 0 && x % y == 0) {
    
    int a;
    int b;
      
    a = x;
    b = y;

    if (a < 0)
      a = -a;
    if (b < 0)
      b = -b;

    int temp;
    while (b != 0) {
      temp = b;
      b = a % b;
      a = temp;
    }   
    
    assert(a == y);
  }
}
