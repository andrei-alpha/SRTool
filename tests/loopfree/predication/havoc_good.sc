
void main(int a, int b)
{
	a = 2;

  if (a == 2) {
    if (b < 2) {
      b = 2;
      assume(a != 3);
    } else {
      assume(a != 4);
      b = 4;
    }
  }	

	// should get new SSA index
	havoc(a);
	assume(a != 2);
	assert(a != 2);
	assert(b > 0);
}
