for i in `seq 1 $1`;
	do
		wine Albert.exe -t -i127.0.0.1 -p16713 &
done

