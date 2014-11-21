
void main() {
  int i;
  int j;  
  int n;

  i = 0;
  n = 1;
  while (i < 200) {
    i = i + 1;
    j = i;
    i = 0;
    assert(!i);
    i = j;
    assert(i);

    n = n * 2;
    if (n > 1024)
      n = 1;
  }

  assert(n);
}
