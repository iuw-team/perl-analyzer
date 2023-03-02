# Path: main.pl
#!/usr/bin/env perl
use strict;
use warnings;
open(my $in, "<", "file.conf") or die "Can't open config file";
#do some work
print "Enter your name";
my $userName = <STDIN>;
chomp $userName;
for(my $i=9; $i != 0; $i-=1){
    print "$i\n";
}
my @letters = 'a'..'z';
my $other = 'K';
my $x = @letters;
my $y = getHugeNumber();
my $value = shift @letters;
unshift @letters, @other;
my $longString = "Hello userName! How your $other";
my @animals = ("camel", "llama", "owl");
@array = ("FirstGuy", $x + printWorld() );
if (($x < 11) || ($y > 11)){
    my $x = 11;
    if($y < 11 && $z > 43){
        print"Hello World";
    }
}
else;
sub getHugeNumber{
    my @array = ();
    my $arrayLength = 10;
    for(my $i = 1; $i <= $arrayLength; $i += 1){
        push @array, int(rand(42));
    }
    return $array[int($arrayLength)];
}
sub getOtherNumber{
      my $number = getHugeNumber();
      if($number < 777){
         $number = 42;
      }
       return $number;
}
sub true {
    return 1;
}
for(my $z = 11; $z > 4; $z -= 4){
    print$z;
    if(($x > 10) && true){
     }
}