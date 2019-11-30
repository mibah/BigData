function plotMe(V)
    AT_S = sortrows(V,6);
    b = 1:size(AT_S,1);
    a = table2array(V(:,2));
    a(:,2) = b';
    scatter(a(:,2),a(:,1))
end