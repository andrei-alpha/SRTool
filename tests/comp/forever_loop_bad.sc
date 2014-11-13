
void main()
{
  int x;
  int y;
  int c;

  c = 0;
  x = 0;
  y = 4;

  while(1) {
    if (y > 30000) {
      c = (c + 1) % 4;
      x = c;
      y = 4;
    }

    x = x + y;
    y = y + 4;

    assert(x != 30);
  }
}
