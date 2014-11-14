
void main()
{
  int x;
  int y;
  int c;

  c = 0;
  x = 0;
  y = 4;

  while(1) {
    if (y > 2000000000) {
      c = (c + 1) % 30;
      x = c;
      y = 4;
    }

    x = x + (y % 7);
    y = y + 4;

    assert(x != 17);
  }
}
