
void main(int n) {
  
  int i;
  int p1;
  int p2;
  p1 = 1;
  p2 = 1; 

  while (n > 0) {

    i = 1;
    while (i < n) {
      p1 = p1 * 2;
      i = i * 2;
    }

    i = 1;
    while (i < n) {
      p2 = p2 * 2;
      i = i * 3;
    }
    
    n = n / 2;
    assert(p1 == p2);
  }
}
