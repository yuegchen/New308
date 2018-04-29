/*

USPRSR			GOP vote for POTUS
USPRSDFL		DEM vote for POTUS
USPRSTOTAL		US President total votes
USREPR			US Representative Republican Party candidate votes
USREPDFL		US Representative Democratic-Farmer-Labor Party candidate votes
USREPTOTAL		US Representative total votes
MNSENR			MN Senator Republican Party candidate votes
MNSENDFL		MN Senator Democratic-Farmer-Labor Party candidate votes
MNSENWI			MN Senator Write In candidate votes--includes registered and non-registered write-ins
MNSENTOTAL		MN Senator total votes
MNLEGR			MN Representative Republican Party candidate votes
MNLEGDFL		MN Representative Democratic-Farmer-Labor Party candidate votes
MNLEGWI			MN Representative Write In candidate votes--includes registered and non-registered write-ins
MNLEGTOTAL		MN Representative total votes

*/

// POTENTIAL FINAL FORMAT
// Create list of multiple sources of data on election results
// Each has a name(for selection), a filename(JSON file containing data), a GOP votes stat,
// a DEM votes stat, a TOTAL votes stat, a precinct name stat(for display), a default
// congressional district number(for default assignment).

// Could also format it so that each source can select congressional VS presidential.

// CURRENT FORMAT
// Start with precinct name, end with Original District.

var stateSyntax = {
	"Precinct Name" : "PCTNAME",
	"Precinct ID" : "VTDID",
	"Municipality" : "MCDNAME",
	"County" : "COUNTYNAME",
	"Republican vote" : "USPRSR",
	"Democrat vote" : "USPRSDFL",
	"Total Vote" : "USPRSTOTAL",
	"Original District" : "CONGDIST", 
};