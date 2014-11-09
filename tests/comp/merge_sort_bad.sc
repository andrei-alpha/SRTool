
void main(int v1, int v2, int v3) {

  int temp;
  int temp2;
  int i; 
  i = 0;

  // This won't work for 1,3,2

  while (i < 4) {
    if (i == 1 && v1 > v2) {
      temp = v1;
      v1 = v2;
      v2 = temp;
    }
    if (i == 2 && v1 > v3) {
      temp = v1;
      temp2 = v2;
      v1 = v3;
      v2 = temp;
      v3 = temp2;
    }

    i = i + 1;
  }  

  assert(v1 <= v2 && v2 <= v3);
}
