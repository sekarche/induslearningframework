%Descriptor Grammar
%
% @targetType URI(a rdf type)
%
% @target attribute_name
%
% @attribute name
% [URI(s) separated by commas]
% [NOMINAL|ENUM|BINNED]=[?|values separated by commas]
% aggregator=[NONE|INDEPENDENT_VAL|COUNT|AVG|MIN|MAX]
%
%Remark: ? applies to NOMINAL and ENUM only,
%        in which case a query is posed to retrieve all possible values from the data source
%
%Remark: Number is always binned
%Remark: If BINNED Option the format is range (of integers) seperated by commas
% Example: 3,5,10
% Bin index:
%  (-infty, 3) = 0
%  [3, 5) = 1
%  [5, 10) = 2
%  [10, +infty) = 3
%
% Example: 5
% Bin index:
%  (-infty, 5) = 0
%  [5, +infty) = 1
%
%Remark:It is assumed that the AVG,MIN,MAX are applicable to numerical datatype only.
%TODO add prefix support