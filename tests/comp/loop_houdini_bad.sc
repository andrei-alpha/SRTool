
void main(int n) {
    
  int i;
  i = 0;
  while (i < n) {
    
    int j;
    while (j > 0) {
      j /= 2;
    }

    i = i + 1;
    assert(j == 0);
    assert(i == 1);
  }
}
