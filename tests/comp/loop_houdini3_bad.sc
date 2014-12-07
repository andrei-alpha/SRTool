
void main(int n) {
  int p1;
  int p2;

  int i;
  int j;
  i = 0;
  j = 0;
  while(i < n) {
    int x1;
    int x2;
    x1 = 0;
    x2 = 0;

    while (p1 > 0) {
      x1 = x1 + 1;
      p1 = p2 / 2;
    }

    while (p2 > 0) {
      x2 = x2 + 1;
      p2 = p2 / 3;
    }

    assert(x1 == x2);
    i = i + 1;
  }
}
