void main(int x, int y) {
  
  if (x < 50) {
    assume(y > 50);
  } 
  else {
    assume(y < 50);
  }

  assert(x != y);
}
