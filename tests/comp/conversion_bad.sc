
void main(int x) {
  int y;
  y = 0;

  int c;
  c = 0;

  // Don't consider last bits
  while(c < 30) {
    int bit;
    bit = 1 << c;

    if (x & bit)
      y = y + bit;

    c = c + 1;
  }

  assert(x == y);
}
