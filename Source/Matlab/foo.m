while start<finish
    snk.Request(cm,start,duration,'absolute');
    cmi=snk.Fetch(5000);
    if (cmi.NumberOfChannels > 0)
        time=cmi.GetTimes(0);
        data=cmi.GetDataAsByteArray(0);
        for i=1:length(time)
            cmo.PutTime(time(i),0);
            cmo.PutDataAsByteArray(0,data(i,:));
            src.Flush(cmo);
        end
    end
    start=start+duration;
end