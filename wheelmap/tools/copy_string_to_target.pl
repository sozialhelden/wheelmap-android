#!/usr/bin/perl

print scalar @ARGV;
if ( scalar @ARGV != 2 ) {
    print "usage:\n";
    print $0 . " <lang_files.zip> <project_res_dir>\n";
    exit(0);
}

my $STRING_FILE=$ARGV[0];
my $PROJECT_RES_DIR=$ARGV[1];

my $TMP_DIR="tmp";



system( "unzip -o -d $TMP_DIR $STRING_FILE");

open( FILES, "find $TMP_DIR -name strings.xml |");

while( <FILES> ) {
  my $file = $_;
  copy_file_to_target( $file );

}

sub copy_file_to_target() {
    my $srcFilePath = $_[0];
    chomp( $srcFilePath );
    # print $srcFile . "\n";
    $srcFilePath =~ m/.*\/(..)\/(.*)/;
    my $targetLang = $1;
    my $fileName = $2; 
    # print $targetLang . " ". $fileName . "\n";

    my $targetDir = "/values";
    if ( $targetLang ne "en" ) {
      $targetDir = $targetDir . "-" . $targetLang;
    } 
    # print $targetDir . "\n";
      		  
    my $targetDirPath = $PROJECT_RES_DIR . $targetDir;

    if ( !-e $targetDirPath &&  !-d $targetDirPath ) {
      print $targetDirPath . " does not exist. Creating.\n";
      mkdir $targetDirPath
    } else {
      print $targetDirPath . " does exist as directory. Fine.\n";
    }

    print "Rewrite file to remove empty strings\n";
    my $rewrittenFilePath = $srcFilePath . "-rewritten";
    system( "cat $srcFilePath | grep -v '><\/' > $rewrittenFilePath" );
   
    my $targetFilePath = $targetDirPath . "/" . $fileName;
    if ( !-e $targetFilePath ) {
      print $targetFilePath . " does not exist. Creating.\n";
      system( "cp $rewrittenFilePath $targetFilePath" );
    } else {
      print $targetFilePath . " does exist. Overwriting.\n";
      system( "cp -f $rewrittenFilePath $targetFilePath" );
    }
}


