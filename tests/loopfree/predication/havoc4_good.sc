
void main(int x) {
  x = 0;
  if (x) {
    havoc(x);
  }
  assert(!x);  
}
