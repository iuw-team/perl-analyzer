#!/usr/bin/env perl
use strict;
use warnings;
open(my $in, "<", "animals.back") or die "Can't open config file";
#do some work
print "Enter your name";
my $userName = <STDIN>;
chomp $userName;
for(my $i=9; $i != 0; $i-=1){
    print "$i\n";
    if($i == 4){
        print "Almost";
    }
}
my $other = <$in>;
my $longString = "Hello $userName! How your $other";
my @animals = ("camel", "llama", "owl");
my $x = @animals;
my $y = getHugeNumber();
my $value = shift @animals;
unshift @animals, $other;
if (($x < 11) || ($y > 11)){
    if($y < 11 && $z > 43){
        print"Let's me do some magic";
    }
}
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