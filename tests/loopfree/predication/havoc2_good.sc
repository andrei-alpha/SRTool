
void main(int a, int b)
{
  assume(a == 2);
  if (b < 2) {
    assert(a == 2);
    a = 2;
  } else {
    assert(a == 2);
  }
  assume(a == 3);
  assert(b == 5);
}
