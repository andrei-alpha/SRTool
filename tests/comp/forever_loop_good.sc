
void main(int v) {
  int x;
  int y;
  assume(v > 0 && v < 1000);

  x = 1;
  y = 1;

  while(1) {
    if (x > 2000000000 || y > 2000000000) {
      x = 1;
      y = 1;
    }

    x = x + 2 * v;
    y = y + 2 * v;

    assert(x + y != 1);
  }
}
