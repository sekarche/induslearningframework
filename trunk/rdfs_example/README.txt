%Descriptor Grammar
%
% @instance_var x
% @value_var a
% @hierarchy_var z
%
% @targetType URI(a rdf type)
%
% @target attribute_name
%
% @attribute name
% [NOMINAL|ENUM|BINNED|NUMERIC]=[?|values separated by commas]
% aggregator=[HISTOGRAM|COUNT|SUM|AVG|MIN|MAX]
% [hierarchy=URI]
% [RBCOptimize=[true|false]]
% {
% Graph pattern
% }
%
%Remark: ? applies to NOMINAL and ENUM only,
%        in which case a query is posed to retrieve all possible values from the data source
%
%Remark: NUMERIC type requires no value specification
%
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
%Remark: It is assumed that the SUM,AVG,MIN,MAX are applicable to numerical datatype only.
%        It is assumed that the HISTOGRAM is applicable to nominal and enum datatype only.
