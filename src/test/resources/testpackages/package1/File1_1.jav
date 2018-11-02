package testpackages.package1;

import testpackages.package2.File2_1;

import testpackages.package4.File4_1;
import testpackages.package4.File4_2;
import testpackages.package4.File4_3;
import testpackages.package4.File4_4;
import testpackages.package4.File4_5;

/*Expecting 4 cycles:
1-2-1
1-2-3-1
1-2-4-3-1
1-4-3-1*/

@SuppressWarnings("unused")
public class File1_1
{
	
}
