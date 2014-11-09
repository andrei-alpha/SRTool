// Merge sort for 4 numbers

void main(int v1, int v2, int v3, int v4) {
  int temp;
  int temp2;
  int i;

  i = 0;

  while (i < 7) {
    if (i == 1 && v1 > v2) {
      temp = v1;
      v1 = v2;
      v2 = temp;
    }
    if (i == 2 && v3 > v4) {
      temp = v3;
      v3 = v4;
      v4 = temp;
    }
    if (i == 3 && v1 > v3) {
      temp = v1;
      temp2 = v2;
      v1 = v3;
      v2 = temp;
      v3 = temp2;
    }
    if (i == 4 && v2 > v3) {
      temp = v2;
      v2 = v3;
      v3 = temp;
    }
    if (i == 5 && v2 > v4) {
      temp = v2;
      temp2 = v3;
      v2 = v4;
      v3 = temp;
      v4 = temp2;
    }
    if (i == 6 && v3 > v4) {
      temp = v3;
      v3 = v4;
      v4 = temp;
    }

    i = i + 1;
  }
  
  assert(v1 <= v2 && v2 <= v3 && v3 <= v4);
}

