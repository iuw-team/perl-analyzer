#!/usr/bin/env perl
use strict;
use warnings;
my %table = (key_0 => 'value_0',
             key_1 => 'value_1',
             key_2 => (1, 3, 5));
foreach my $value (%table){
    for(my $i = 1; $i < 10; $i += 1){
        for(my $j = 1; $j < 100; $j += 3){
            my $state = 0;
            my $temp = 0;
            for(my $k = 2; $k < 1000 && $state != 1; $k += 1){
                $temp = hardCalc($i, $j, $k);
                if($temp > 100){
                    $temp = 1;
                }
                elsif ($temp > 50){
                    print "Almost'";
                }
                elsif ($temp > 10){
                    print "Very far";
                }
                elsif ($temp < 2){
                    print "Too small!";
                }
                else {
                    print "Not now";
                }
            }
        }
    }
}
my ($x, $y, $z) = (0, 12, 55);
if (($x < 11) || ($y > 11)){
    if($y < 11 && $z > 43){
        print"Let's me do some magic";
    }
    elsif(($x > 11) && ($z > 12)){
        print "Another text";
    }
    elsif(getHugeNumber() > 11) {
        print "Are you crazy?)";
    }
    else {
        print "Finally!";
    }
}