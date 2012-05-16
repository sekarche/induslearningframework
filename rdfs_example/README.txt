%Descriptor Grammar
%
% @instance_var ?x
% @value_var ?a
% @hierarchy_var ?z
%
% @targetType URI(a rdf type)
%
% @target attribute_name
%
% @attribute name
% [NOMINAL|ENUM|BINNED|NUMERIC]=[?|values separated by commas|type of distribution for numeric]
% aggregator=[NONE|HISTOGRAM|COUNT|SUM|AVG|MIN|MAX]
% hierarchyRoot=[URI]
% {
% Graph pattern
% }
%
%Remark: Target attribute applies to NOMINAL and ENUM only with no aggregator
%
%Remark: ? applies to NOMINAL and ENUM only,
%        in which case a query is posed to retrieve all possible values from the data source
%
%Remark: types of distribution for NUMERIC are [POISSON,EXPONENTIAL,GAUSSIAN]
%
%Remark: If BINNED Option the format is range (of integers) separated by commas
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
%Remark: The SUM,AVG,MIN,MAX are applicable to numerical datatype only.
%        The HISTOGRAM is applicable to nominal and enum datatype only.
